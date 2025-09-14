package com.zenyte.discord.listeners.command.impl

import com.zenyte.common.generateRandomString
import com.zenyte.common.datastore.RedisCache
import com.zenyte.discord.DiscordBot
import com.zenyte.discord.listeners.command.Command
import net.dv8tion.jda.api.entities.Message

/**
 * @author Corey
 * @since 31/05/2019
 */
class VerifyCommand : Command {

    override val identifiers = arrayOf("verify")

    override val description =
        "PMs a code to verify your in-game account with your Discord account."

    override fun execute(message: Message, identifier: String) {
        // Disabled by default; re-enable when verification flow is wired up
        return

        /*
        val user = message.member?.user ?: return
        val code = generateRandomString()

        if (DiscordBot.userIsVerified(message.member!!)) {
            message.channel.sendMessage("${user.asMention} you are already verified!").queue()
            return
        }

        user.openPrivateChannel().queue(
            { privateChannel ->
                privateChannel.sendMessage(
                    "Your code is: `$code`; this will expire in **${CODE_EXPIRATION_TIME / 60} minutes**" +
                        "\nUse `::verify $code` in-game to complete your verification!"
                ).queue(null) {
                    message.channel.sendMessage(
                        "Failed to send message; you need to allow private messages ${user.asMention}!"
                    ).queue()
                }
            },
            {
                message.channel.sendMessage("Could not open private channel to ${user.asMention}.").queue()
            }
        )

        // Store the code in Redis with a 5-minute expiration
        RedisCache.redis.sync().setex(
            "discord_verification_code:$code",
            CODE_EXPIRATION_TIME.toLong(),
            user.id
        )
        */
    }

    companion object {
        private const val CODE_EXPIRATION_TIME = 60 * 5 // 5 minutes
    }
}
