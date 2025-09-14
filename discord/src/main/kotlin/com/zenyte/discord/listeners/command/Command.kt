package com.zenyte.discord.listeners.command

import net.dv8tion.jda.api.entities.Message

/**
 * Represents a Discord bot command.
 *
 * @author Corey
 * @since 07/10/2018
 */
interface Command {

    /** Identifiers which follow the command prefix used to execute the command. */
    val identifiers: Array<String>

    /** The text which displays in the help command. */
    val description: String

    /**
     * Whether the command can be executed – this could
     * be role dependent or something else.
     *
     * @param message The message being used to execute the command.
     * @return Whether or not the command can be executed.
     */
    fun canExecute(message: Message): Boolean = true

    /**
     * Executes the command logic.
     *
     * @param message The message being used to execute the command.
     * @param identifier The identifier used to invoke the command.
     */
    fun execute(message: Message, identifier: String)

    /**
     * Executes the command if [canExecute] returns true.
     *
     * Skips execution if the message was from a webhook or bot.
     */
    fun executeCommand(message: Message, identifier: String) {
        if (message.isWebhookMessage || message.author.isBot) return
        if (canExecute(message)) execute(message, identifier)
    }
}
