//package com.zenyte.discord.listeners.command.impl
//
//import com.zenyte.discord.DiscordBot
//import com.zenyte.discord.listeners.command.Command
//import mu.KotlinLogging
//import net.dv8tion.jda.api.EmbedBuilder
//import net.dv8tion.jda.api.entities.Message
//import java.text.SimpleDateFormat
//import java.util.*
//
///**
// * @author Corey
// * @since 01/06/2019
// */
//class InfoCommand : Command {
//
//    private val logger = KotlinLogging.logger {}
//
//    override val identifiers = arrayOf("info")
//
//    override val description = "Show forum profile info for a verified user."
//
//    override fun execute(message: Message, identifier: String) {
//        val user = message.member?.user ?: return
//
//        if (!DiscordBot.userIsVerified(message.member!!)) {
//            message.channel.sendMessage("${user.asMention} you need to be verified to use this command!").queue()
//            return
//        }
//
//        // TODO allow staff to get info from other users
//
//        try {
//            val memberId = Discord.getVerifiedMemberId(user.idLong)
//            val results = User.getColumnsByMemberId(
//                memberId,
//                arrayOf("name", "pp_main_photo", "joined", "msg_count_total")
//            )
//
//            val name = results.getValue("name")
//            val profilePhoto = "${FORUM_URL}uploads/${results.getValue("pp_main_photo")}"
//            val joinedDate = (results.getValue("joined").toLong() * 1000).toDateString()
//            val messageCount = results.getValue("msg_count_total")
//            val forumUrl = "${FORUM_URL}profile/$memberId-$name/"
//
//            val eb = EmbedBuilder()
//                .setAuthor("${message.member?.effectiveName}'s Profile", forumUrl, user.avatarUrl)
//                .setColor(message.member?.colorRaw ?: 0xF1A2A7)
//                .setThumbnail(profilePhoto)
//                .addField("Name", name, true)
//                .addField("Joined", joinedDate, true)
//                .addField("Post Count", messageCount, true)
//
//            message.channel.sendMessageEmbeds(eb.build()).queue()
//
//        } catch (e: Exception) {
//            message.addReaction("⚠️").queue() // warning emoji
//            logger.error(e) { "Error getting info for '${user.name}'" }
//        }
//    }
//
//    private fun Long.toDateString(): String {
//        val sdf = SimpleDateFormat("dd/MM/yyyy")
//        return sdf.format(Date(this))
//    }
//
//    companion object {
//        private const val FORUM_URL = "https://forums.zenyte.com/"
//    }
//}
