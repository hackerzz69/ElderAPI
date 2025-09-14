package com.zenyte.discord.listeners.command

import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 07/10/2018
 */
interface Command {
    
    /**
     * @return Identifiers which follow the command prefix used to execute the command.
     */
    val identifiers: Array<String>
    
    /**
     * @return The text which displays in the help command.
     */
    val description: String
    
    /**
     * Whether the command can be executed - this could
     * be role dependent or something else.
     *
     * @param message The message being used to execute the command.
     * @return Whether or not the command can be executed.
     */
    fun canExecute(message: Message): Boolean {
        return true
    }
    
    /**
     * @param message The message being used to execute the command.
     */
    fun execute(message: Message, identifier: String)
    
    /**
     * Executes the command if [.canExecute] returns true.
     *
     * @param message The message being used to execute the command.
     */
    fun executeCommand(message: Message, identifier: String) {
        if (message.isWebhookMessage || message.author.isBot) {
            return
        }
        if (canExecute(message)) {
            execute(message, identifier)
        }
    }
    
}
