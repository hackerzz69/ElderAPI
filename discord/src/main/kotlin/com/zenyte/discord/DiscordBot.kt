package com.zenyte.discord

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zenyte.api.model.Role
import BotConfig
import com.zenyte.common.EnvironmentVariable
import com.zenyte.discord.cores.CoresManager
import com.zenyte.discord.listeners.CommandListener
import com.zenyte.discord.listeners.command.Command
import io.github.classgraph.ClassGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.hooks.EventListener
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess
import kotlin.reflect.full.createInstance

/**
 * Main Discord bot singleton.
 */
object DiscordBot {

    val gson: Gson = GsonBuilder().disableHtmlEscaping().create()

    private const val API_PING_TIMEOUT = 15_000
    private const val API_PING_AMOUNT = 10
    private const val ZENYTE_GUILD = "373833867934826496"
    private val DISCORD_BOT_ENV_VAR = EnvironmentVariable("BOT_TOKEN")

    private val logger = KotlinLogging.logger {}
    private val commands = mutableListOf<Command>()

    lateinit var jda: JDA
        private set

    var greetNewMembers: Boolean = true

    fun init() {
        if (Api.DEVELOPER_MODE) {
            logger.info { "Developer mode enabled — disabling member greets" }
            greetNewMembers = false
        }

        preLogin()
        login()
        postLogin()
    }

    private fun preLogin() {
        checkEnvironmentVariables()
        pingApi()
    }

    private fun pingApi() {
        if (Api.DEVELOPER_MODE) {
            logger.info { "Dev mode enabled, skipping API ping" }
            return
        }

        var success = false
        for (i in 1..API_PING_AMOUNT) {
            logger.info { "Pinging API service (attempt $i/$API_PING_AMOUNT)" }
            val ping = try {
                Api.ping()
            } catch (e: ConnectException) {
                logger.warn { "Ping failed: ${e.message}" }
                false
            }

            if (ping) {
                logger.info { "API responded successfully." }
                success = true
                break
            } else {
                val waitSeconds = TimeUnit.MILLISECONDS.toSeconds(API_PING_TIMEOUT.toLong())
                logger.warn { "Ping failed — retrying in ${waitSeconds}s" }
                runBlocking { delay(API_PING_TIMEOUT.toLong()) }
            }
        }

        if (!success) {
            logger.error { "Failed to reach API after $API_PING_AMOUNT attempts" }
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
                logger.error { "Environment variable '${it.key}' is missing/invalid" }
                exitProcess(2)
            } else {
                logger.debug { "Environment variable ${it.key} has a valid value" }
            }
        }
    }

    private fun login() {
        try {
            val token = DISCORD_BOT_ENV_VAR.value
            val builder = JDABuilder.createDefault(token)
                .addEventListeners(*loadListeners().toTypedArray())

            jda = builder.build().awaitReady()
            logger.info { "Discord connection established" }
        } catch (e: LoginException) {
            logger.error(e) { "Failed to login to Discord" }
            exitProcess(2)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.error(e) { "Discord login interrupted" }
            exitProcess(2)
        }
    }

    private fun postLogin() {
        reloadCommands()
        CoresManager.init()
        scheduleTasks(BotConfig.default(), this)
    }

    fun getZenyteGuild(): Guild =
        jda.getGuildById(ZENYTE_GUILD) ?: error("Guild $ZENYTE_GUILD not found!")

    fun userIsVerified(member: Member): Boolean {
        val verifiedRole = getZenyteGuild().getRoleById(Role.VERIFIED.discordRoleId)
        return verifiedRole != null && verifiedRole in member.roles
    }

    fun setPresence(str: String) {
        jda.presence.activity = Activity.playing(str)
    }

    private fun loadListeners(): List<EventListener> {
        val listeners = loadClassesImplementing<EventListener>(
            recursive = false,
            "com.zenyte.discord.listeners"
        )
        logger.info { "Loaded ${listeners.size} listeners: ${listeners.joinToString { it::class.java.simpleName }}" }
        return listeners
    }

    fun getCommands(): List<Command> = commands.toList()

    private fun reloadCommands() {
        commands.clear()
        commands.addAll(loadCommands())
    }

    private fun loadCommands(): List<Command> {
        val commands = loadClassesImplementing<Command>(
            recursive = true,
            "com.zenyte.discord.listeners.command.impl"
        )
        logger.info {
            "Loaded ${commands.size} commands: ${
                commands.joinToString { "${CommandListener.COMMAND_PREFIX}${it.identifiers.joinToString("|")}" }
            }"
        }
        return commands
    }

    private inline fun <reified T : Any> loadClassesImplementing(
        recursive: Boolean,
        vararg dirs: String
    ): List<T> {
        val clazz = T::class.java
        val instances = mutableListOf<T>()

        ClassGraph().enableAllInfo().apply {
            if (recursive) {
                acceptPackages(*dirs)
            } else {
                acceptPackagesNonRecursive(*dirs)
            }
            scan().use { scanResult ->
                for (classInfo in scanResult.getClassesImplementing(clazz.canonicalName)) {
                    try {
                        val loadedClass = classInfo.loadClass()
                        val instance = loadedClass.kotlin.createInstance()
                        instances.add(clazz.cast(instance))
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to load ${classInfo.simpleName}" }
                    }
                }
            }
        }
        return instances
    }
}
