package com.zenyte.api.`in`

import com.zenyte.api.model.*
import com.zenyte.common.datastore.RedisCache
import com.zenyte.common.CommonConfig.gson
import com.zenyte.sql.query.hiscores.HiscoresByColumnQuery
import com.zenyte.sql.query.hiscores.RuneliteHiscoresQuery
import com.zenyte.sql.query.hiscores.UpdateHiscoreExpMode
import com.zenyte.sql.query.hiscores.UpdateSingleHiscore
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * @author Corey
 * @since 04/05/18
 */
@RestController
@RequestMapping("/hiscores")
object HiscoresController {
    
    private enum class Field {
        DISPLAY_NAME,
        MODE,
        EXP_MODE,
        EXPERIENCE,
        JSON
        ;
        
        operator fun plus(other: Any) = "${this.name}:$other"
    }
    
    private val logger = KotlinLogging.logger {}
    
    private fun Int.getHiscoresKey() = "hiscores:$this"
    
    private fun Array<SkillHiscore>.getRedisKey() = "hiscores:${this.getUserId()}"
    private fun Array<SkillHiscore>.getDisplayNameLower() = this[0].username.toLowerCase()
    private fun Array<SkillHiscore>.getDisplayName() = this[0].username
    private fun Array<SkillHiscore>.getUserId() = this[0].userId
    private fun Array<SkillHiscore>.getGameMode() = this[0].mode
    private fun Array<SkillHiscore>.getExpMode() = this[0].expMode
    
    private fun String.displayNameToIdKey() = "hiscores_user_id:${this.toLowerCase().replace(" ", "_")}"
    
    
    @GetMapping("/user/{displayName}", produces = ["application/json"])
    fun getHiscore(@PathVariable displayName: String): String? {
        val userId = userIdFromDisplayName(displayName) ?: return null
        return RedisCache.redis.sync().hget(userId.getHiscoresKey(), Field.JSON.toString())
    }
    
    @PostMapping("/user/{displayName}/update")
    fun updateHiscore(@PathVariable displayName: String, @RequestBody hiscores: Array<SkillHiscore>) {
        logger.info { "Updating hiscores for '$displayName'" }
        updateDatabase(hiscores)
        updateCache(hiscores)
        // TODO detect updated display name
    }
    
    @PostMapping("/user/{displayName}/update/expmode")
    fun updatePlayerExpMode(@PathVariable displayName: String, @RequestBody changes: ExpModeUpdate) {
        logger.info { "Updating exp mode for '$displayName'" }
        val userId = userIdFromDisplayName(displayName)
                ?: throw RuntimeException("Couldn't find user id for: $displayName")
        
        logger.info { "Updating database" }
        val (_, exception) = UpdateHiscoreExpMode(userId, changes).getResults()
    
        if (exception == null) {
            logger.info { "Updating redis cache" }
            RedisCache.redis.sync().hset(userId.getHiscoresKey(), Field.EXP_MODE.toString(), changes.newMode.index.toString())
        } else {
            exception.printStackTrace()
        }

    }
    
    @PostMapping("/user/{displayName}/update/gamemode")
    fun updateGameMode(@PathVariable displayName: String, @RequestBody changes: GameModeUpdate) {
        logger.info { "Updating game mode for '$displayName'" }
        val userId = userIdFromDisplayName(displayName)
                ?: throw RuntimeException("Couldn't find user id for: $displayName")


//        updateDatabase(hiscores)
//        updateCache(hiscores)
        // TODO detect updated display name
    }
    
    @PostMapping("/user/{oldDisplayName}/updatename")
    fun updateUserDisplayName(@PathVariable oldDisplayName: String, @RequestBody newDisplayName: String) {
        logger.info { "Updating hiscores display name: '$oldDisplayName' -> '$newDisplayName'" }
        
        val userId = userIdFromDisplayName(oldDisplayName)
        if (userId == null) {
            logger.error { "Tried to update invalid display name: '$oldDisplayName' -> '$newDisplayName'" }
            return
        }
    
        RedisCache.redis.sync().del(oldDisplayName.displayNameToIdKey())
        RedisCache.redis.sync().set(newDisplayName.displayNameToIdKey(), userId.toString())
        
        // TODO update rows with old username
    }
    
    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User not found")
    class UserNotFoundException : RuntimeException()
    
    @GetMapping("/user/{displayName}/runelite")
    fun getRuneliteFormat(@PathVariable displayName: String, @RequestParam(defaultValue = "-1") ironman: Int, @RequestParam(defaultValue = "-1") expMode: Int): String {
        val runeliteKey = "runelite:${displayName.toLowerCase()}:$ironman:$expMode"
        val runeliteCache = RedisCache.redis.sync().get(runeliteKey)
    
        return if (runeliteCache.isNullOrBlank()) {
            val (result, exception) = RuneliteHiscoresQuery(displayName, IronmanMode.VALUES.firstOrNull { it.id == ironman }, ExpMode.VALUES.firstOrNull { it.index == expMode }).getResults()
            result as RuneliteHiscoresQuery.RuneliteHiscoreResult
        
            if (result.response.isNullOrBlank()) {
                throw UserNotFoundException()
            } else if (exception != null) {
                throw exception
            }
    
            RedisCache.redis.sync().setex(runeliteKey, 60 * 5, result.response) // cache runelite result for 5 mins
            return result.response
        } else {
            runeliteCache
        }
    }
    
    @GetMapping("/overall/{measurement}")
    fun getOverallHiscores(@PathVariable measurement: String, @RequestParam(defaultValue = "1") page: Int, @RequestParam(defaultValue = "-1") ironman: Int, @RequestParam(defaultValue = "-1") expMode: Int): HiscoresByColumn {
        val (result, exception) = HiscoresByColumnQuery(measurement, page, IronmanMode.VALUES.firstOrNull { it.id == ironman }, ExpMode.VALUES.firstOrNull { it.index == expMode }).getResults()
        result as HiscoresByColumnQuery.HiscoresByColumnResult
        
        if (result.result == null) {
            throw UserNotFoundException()
        } else if (exception != null) {
            throw exception
        }
        
        return result.result
    }
    
    @GetMapping("/user/{displayName}/gamemode")
    fun getGameMode(@PathVariable displayName: String): Int {
        val userId = userIdFromDisplayName(displayName) ?: return -1
        return getGameMode(userId) ?: -1
    }
    
    @GetMapping("/user/{displayName}/gamemode/name")
    fun getGameModeName(@PathVariable displayName: String): String {
        val gameMode = getGameMode(displayName)
        if (gameMode < 0) return "INVALID"
        return IronmanMode.VALUES.firstOrNull { it.id == gameMode }?.name ?: "INVALID"
    }
    
    @GetMapping("/user/{displayName}/expmode")
    fun getExpMode(@PathVariable displayName: String): Int {
        val userId = userIdFromDisplayName(displayName) ?: return -1
        return getExpMode(userId) ?: -1
    }
    
    @GetMapping("/user/{displayName}/expmode/name")
    fun getExpModeName(@PathVariable displayName: String): String {
        val expMode = getExpMode(displayName)
        if (expMode < 0) return "INVALID"
        return ExpMode.VALUES.firstOrNull { it.index == expMode }?.name ?: "INVALID"
    }
    
    private fun getGameMode(userId: Int): Int? {
        return RedisCache.redis.sync().hget(userId.getHiscoresKey(), Field.MODE.toString())?.toInt()
    }
    
    private fun getExpMode(userId: Int): Int? {
        return RedisCache.redis.sync().hget(userId.getHiscoresKey(), Field.EXP_MODE.toString())?.toInt()
    }
    
    private fun updateDatabase(hiscores: Array<SkillHiscore>) {
        val gameMode = hiscores.getGameMode().id
        val userId = hiscores.getUserId()
        val username = hiscores.getDisplayName()
        val expMode = hiscores.getExpMode().index
    
        logger.info { "Updating hiscores database; user=$username, total_level=${hiscores.getTotalLevel().level}, mode=$gameMode, xp_mode=$expMode" }
        logger.info { hiscores.getTotalLevel() }
        
        val rowsToUpdate = hiscores.plus(hiscores.getTotalLevel()).filter {
            val cachedExp = RedisCache.redis.sync().hget(hiscores.getRedisKey(), Field.EXPERIENCE + it.skillId)
                    ?: return@filter true
            return@filter it.experience != cachedExp.toLong()
        }
    
        if (rowsToUpdate.isNotEmpty()) {
            RedisCache.redis.sync().del("runelite:${hiscores.getDisplayNameLower()}") // reset the runelite cache
            val (_, exception) = UpdateSingleHiscore(userId, gameMode, expMode, username, rowsToUpdate).getResults()
            exception?.printStackTrace()
        }
        
    }
    
    private fun Array<SkillHiscore>.getTotalLevel(): SkillHiscore {
        return SkillHiscore(this.getUserId(),
                this.getDisplayName(),
                this.getGameMode(),
                this.getExpMode(),
                Skill.TOTAL.id,
                Skill.TOTAL.formattedName,
                this.map { it.level }.sum(),
                this.map { it.experience }.sum()
        )
    }
    
    private fun updateCache(hiscores: Array<SkillHiscore>) {
        val hiscoresWithTotal = hiscores.plus(hiscores.getTotalLevel())
    
        RedisCache.redis.sync().hmset(hiscores.getRedisKey(), mapOf(
                Field.JSON.toString() to gson.toJson(hiscoresWithTotal),
                Field.DISPLAY_NAME.toString() to hiscores.getDisplayName(),
                Field.EXP_MODE.toString() to hiscores.getExpMode().index.toString(),
                Field.MODE.toString() to hiscores.getGameMode().id.toString()
        ).plus(hiscoresWithTotal.map { Pair(Field.EXPERIENCE + it.skillId, it.experience.toString()) }))
        RedisCache.redis.sync().set(hiscores.getDisplayNameLower().displayNameToIdKey(), hiscores.getUserId().toString())
    
    }
    
    private fun userIdFromDisplayName(displayName: String): Int? {
        return RedisCache.redis.sync().get(displayName.displayNameToIdKey())?.toInt()
    }
    
}