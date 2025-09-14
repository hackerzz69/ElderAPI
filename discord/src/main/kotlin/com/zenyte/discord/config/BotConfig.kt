data class BotConfig(
    val guildId: Long,
    val eventAnnouncementChannels: List<Long> = emptyList(),
    val eventRoleIds: List<Long> = emptyList()
) {
    companion object {
        fun default() = BotConfig(
            guildId = 373833867934826496,
            eventAnnouncementChannels = listOf(671146510917959691, 677272449213005835),
            eventRoleIds = listOf(671148089188417567, 677253546801889290)
        )
    }
}
