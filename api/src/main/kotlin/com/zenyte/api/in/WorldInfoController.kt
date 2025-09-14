package com.zenyte.api.`in`

import com.zenyte.api.model.World
import com.zenyte.api.model.WorldEvent
import com.zenyte.common.WorldInfo as WorldInfoUtil
import com.zenyte.common.WorldInfo.getKey
import com.zenyte.common.datastore.RedisCache
import com.zenyte.common.CommonConfig.gson
import com.zenyte.sql.query.game.WorldEventsQuery
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import mu.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * World info REST endpoints.
 *
 * @author Corey
 * @since 01/05/18
 */
@RestController
@RequestMapping("/worldinfo")
class WorldInfoController {

    private val logger = KotlinLogging.logger {}

    /**
     * How long world info is cached for (seconds).
     */
    private companion object {
        const val WORLD_CACHE_TIME = 60 * 5
    }

    @GetMapping("/all/slr", produces = ["application/octet-stream"])
    fun binaryList(): ResponseEntity<Resource> {
        val encodedWorlds = encodeWorldList(getAllWorlds().toList())

        val buf = Unpooled.buffer().apply {
            writeInt(encodedWorlds.readableBytes())
            writeBytes(encodedWorlds)
        }

        val headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=slr.ws")
            add(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
            add(HttpHeaders.CONTENT_LENGTH, buf.readableBytes().toString())
        }

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(buf.readableBytes().toLong())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(ByteArrayResource(buf.array()))
    }

    private fun encodeWorldList(list: List<World>): ByteBuf {
        fun ByteBuf.writeString(value: String) {
            val bytes = value.toByteArray()
            writeBytes(bytes)
            writeByte(0) // null terminator
        }

        val buf = Unpooled.buffer()
        buf.writeShort(list.size)

        list.sortedBy { it.id }.forEach { world ->
            var mask = 0
            for (flag in world.flags) {
                mask = if (flag.override) {
                    flag.mask
                } else {
                    mask or flag.mask
                }
            }

            buf.apply {
                writeShort(world.id)
                writeInt(mask)
                writeString(world.address)
                writeString(world.activity)
                writeByte(world.location.id)
                writeShort(world.playerCount)
            }
        }

        return buf
    }

    @PostMapping("/world/update")
    fun updateWorld(@RequestBody world: World) {
        val worldJson = gson.toJson(world)

        val redis = RedisCache.redis.sync()
        // hmset is deprecated, replace with hset
        redis.hset(world.getKey(), mapOf(
            WorldInfoUtil.Field.JSON.toString() to worldJson,
            WorldInfoUtil.Field.COUNT.toString() to world.playerCount.toString(),
            WorldInfoUtil.Field.UPTIME.toString() to world.uptime.toString(),
            WorldInfoUtil.Field.PLAYERS.toString() to world.playersOnline.toString()
        ))
        redis.expire(world.getKey(), WORLD_CACHE_TIME.toLong())

        logger.info { "Updated world info for ${world.name}" }
    }

    @GetMapping("/all", produces = ["application/json"])
    fun getAllWorlds(): Array<World> =
        WorldInfoUtil.getAllWorlds().toTypedArray()

    @GetMapping("/all/count")
    fun getTotalPlayerCount(): Int =
        WorldInfoUtil.getTotalPlayerCount()

    @GetMapping("/world/{name}", produces = ["application/json"])
    fun getWorld(@PathVariable name: String): String? =
        WorldInfoUtil.getWorld(name)

    @GetMapping("/world/{name}/online")
    fun isOnline(@PathVariable name: String): Boolean =
        WorldInfoUtil.isOnline(name)

    @GetMapping("/world/{name}/count")
    fun getPlayerCountForWorld(@PathVariable name: String): String? =
        WorldInfoUtil.getPlayerCountForWorld(name)?.toString()

    @GetMapping("/world/{name}/uptime")
    fun getWorldUptime(@PathVariable name: String): String? =
        WorldInfoUtil.getWorldUptime(name)?.toString()

    @GetMapping("/world/{name}/players", produces = ["application/json"])
    fun getPlayersForWorld(@PathVariable name: String): Array<String> =
        WorldInfoUtil.getPlayersForWorld(name).toTypedArray()

    @GetMapping("/world/{name}/events", produces = ["application/json"])
    fun getEventsForWorld(@PathVariable name: String): ArrayList<WorldEvent>? {
        val (results, exception) = WorldEventsQuery(name).getResults()
        if (exception != null) {
            logger.error(exception) { "Failed to fetch events for world $name" }
            return null
        }
        return (results as WorldEventsQuery.WorldEventResult).events
    }
}
