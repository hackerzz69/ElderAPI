package com.zenyte.api.model

data class DiscordMessage(val channelId: String, val content: String)

//data class DiscordUser(private val user: User,
//                       val id: String = user.id,
//                       val name: String = user.name,
//                       val registered: OffsetDateTime = user.creationTime)
