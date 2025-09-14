package com.zenyte.discord

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zenyte.api.model.Role
import com.zenyte.common.EnvironmentVariable
import com.zenyte.discord.cores.CoresManager
import com.zenyte.discord.listeners.CommandListener
import com.zenyte.discord.listeners.command.Command
import io.github.classgraph.ClassGraph
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.hooks.EventListener
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

/**
 * @author Corey
 * @since 06/10/2018
 */
object DiscordBot {
    
    val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    
    private const val API_PING_TIMEOUT = 15_000
    private const val API_PING_AMOUNT = 10
    private const val ZENYTE_GUILD = "373833867934826496"
    private val DISCORD_BOT_ENV_VAR = EnvironmentVariable("BOT_TOKEN")
    
    private val logger = KotlinLogging.logger {}
    private val commands = ArrayList<Command>()
    
    lateinit var jda: JDA
    
    var greetNewMembers = true
    
    fun init() {
        if (Api.DEVELOPER_MODE) {
            logger.info { "Developer mode enabled" }
            greetNewMembers = false
        }
    
        preLogin()
        login()
        postLogin()
    }
    
    private fun preLogin() {
        checkEnvironmentVariables()
        ping()
    }
    
    private fun ping() {
        if (Api.DEVELOPER_MODE) {
            logger.info { "Dev mode enabled, skipping api ping" }
            return
        }
    
        var success = false
    
        for (i in 1..API_PING_AMOUNT) {
            val apiUrl = Api.getApiRoot()
                .addPathSegment("ping")
                .build()

            // Log the URL
            logger.info ("Request URL: $apiUrl")

            logger.info { "Pinging api service; attempt $i" }

            val ping = try {
                Api.ping()
            } catch (e: ConnectException) {
                logger.warn { "Failed to ping, reason: ${e.cause} / ${e.message}" }
                false
            }
            
            if (!ping) {
                logger.warn { "Ping failed - retrying in ${TimeUnit.MILLISECONDS.toSeconds(API_PING_TIMEOUT.toLong())}s" }
                Thread.sleep(API_PING_TIMEOUT.toLong())
            } else {
                logger.info { "Received ping response from api server." }
                success = true
                break
            }
        }
        
        if (!success) {
            logger.error { "Failed to ping api after $API_PING_AMOUNT attempts" }
            exitProcess(2)
        }
        
    }
    
    private fun checkEnvironmentVariables() {
        val environmentVariables = listOf(
                if (!Api.DEVELOPER_MODE) Api.API_TOKEN_ENV_VAR else null,
                if (!Api.DEVELOPER_MODE) Api.API_URL_ENV_VAR else null,
                DISCORD_BOT_ENV_VAR
        )
    
        environmentVariables.filterNotNull().forEach {
            if (it.value.isNullOrBlank()) {
                logger.error { "Environment variable '$it' invalid!" }
                exitProcess(2)
            } else {
                logger.debug { "Environment variable $it has valid value" }
            }
        }
    }
    
    private fun login() {
        try {
            val builder = JDABuilder(DISCORD_BOT_ENV_VAR.value)
                    .addEventListeners(*loadListeners().toTypedArray())
            jda = builder.build()
            jda.awaitReady() // block until connection has been made
        } catch (e: LoginException) {
            logger.error(e) { "Failed to login" }
            exitProcess(2)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    private fun postLogin() {
        reloadCommands()
        CoresManager.init()
        scheduleTasks()
    }
    
    fun getZenyteGuild(): Guild = jda.getGuildById(ZENYTE_GUILD)!!
    
    fun userIsVerified(member: Member): Boolean {
        val verifiedRole = getZenyteGuild().getRoleById(Role.VERIFIED.discordRoleId)
        return member.roles.contains(verifiedRole)
    }
    
    fun setPresence(str: String) {
        jda.presence.activity = Activity.playing(str)
    }
    
    private fun loadListeners(): List<EventListener> {
        val listeners = loadClassesImplementing<EventListener>(false, "com.zenyte.discord.listeners")
        logger.info { "Loaded ${listeners.size} listeners:" }
        listeners.forEach {
            logger.info { it::class.java.simpleName }
        }
        return listeners
    }
    
    /**
     * @return A copy of the commands list.
     */
    fun getCommands(): List<Command> {
        return ArrayList(commands)
    }
    
    private fun reloadCommands() {
        commands.clear()
        commands.addAll(loadCommands())
    }
    
    private fun loadCommands(): List<Command> {
        val commands = loadClassesImplementing<Command>(true, "com.zenyte.discord.listeners.command.impl")
        logger.info { "Loaded ${commands.size} commands:" }
        commands.forEach {
            logger.info {
                "${CommandListener.COMMAND_PREFIX}${it.identifiers.joinToString("|")} (${it::class.java.simpleName})"
            }
        }
        return commands
    }
    
    /**
     * Returns loaded instances of all classes found implementing the provided class in the provided packages.
     *
     * @param recursive Whether or not it should recurse through packages.
     * @param dirs      The packages to look in.
     * @param <T>       The type we want to look for and return.
     * @return An [ArrayList] of instantiated objects.
     */
    private inline fun <reified T : Any> loadClassesImplementing(recursive: Boolean, vararg dirs: String): List<T> {
        val classes = ArrayList<T>()
        val clazz = T::class.java
    
        ClassGraph().enableAllInfo().apply {
            if (recursive) {
                whitelistPackages(*dirs)
            } else {
                whitelistPackagesNonRecursive(*dirs)
            }
        
            scan().use { classesScanned ->
                for (classInfo in classesScanned.getClassesImplementing(clazz.canonicalName)) {
                    try {
                        classes.add(clazz.cast(classInfo.loadClass().newInstance()))
                    } catch (e: InstantiationException) {
                        logger.error(e) { "Failed to load ${classInfo.simpleName}" }
                    } catch (e: IllegalAccessException) {
                        logger.error(e) { "Failed to load ${classInfo.simpleName}" }
                    }
                }
            }
        }
    
        return classes
    }
    
}
