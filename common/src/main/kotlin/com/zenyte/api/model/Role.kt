package com.zenyte.api.model

enum class IronmanMode(val id: Int) {
    REGULAR(0),
    IRONMAN(1),
    ULTIMATE_IRONMAN(2),
    HARDCORE_IRONMAN(3),
    DEAD_HARDCORE_IRONMAN(4);
    
    companion object {
        @JvmField
        val VALUES = values().asList()
    }
}

enum class ExpMode(val index: Int) {
    FIFTY(0),
    TEN(1),
    FIVE(2);
    
    companion object {
        @JvmField
        val VALUES = values().asList()
    }
}

enum class Role(val forumGroupId: Int = -1, val discordRoleId: Long = -1) {
    
    QUEEN(12, 374174447730556928),
    ADMINISTRATOR(4, 374174266960379905),
    DEVELOPER(11, 378218219326013443),
    
    REGISTERED_MEMBER(forumGroupId = 3),
    
    WEB_DEVELOPER(19, 573618268724658226),
    FORUM_MODERATOR(7, 581282007389306880),
    MODERATOR(6, 402904089886851082),
    SENIOR_MODERATOR(20, 584558360955977779),
    SUPPORT(8, 402904374873161730),
    
    STAFF(discordRoleId = 378650181165514752),
    
    YOUTUBER(18, 572801385155002386),
    
    SAPPHIRE_MEMBER(14, 582400825352257566),
    EMERALD_MEMBER(15, 582401068592529448),
    RUBY_MEMBER(16, 582401279285002240),
    DIAMOND_MEMBER(17, 582401511548780564),
    DRAGONSTONE_MEMBER(21, 583010349385777152),
    ONYX_MEMBER(22, 583010520441815041),
    ZENYTE_MEMBER(23, 583010557075128320),
    
    VERIFIED(discordRoleId = 582579215199895553),
    BETA_CREW(discordRoleId = 426449738108370944),
    BETA_MEDAL(discordRoleId = 449849891779379200),
    
    ZENYTE_BOT(discordRoleId = 503995939783180288),
    
    ;
    
    fun isDiscordRole() = discordRoleId != -1L
    fun isForumRole() = forumGroupId != -1
    
    companion object {
        @JvmField
        val VALUES = values().asList()
        
        @JvmField
        val FORUM_GROUPS = VALUES.filter { it.isForumRole() }.map { Pair(it.forumGroupId, it) }.toMap()
        
        @JvmField
        val DISCORD_ROLES = VALUES.filter { it.isDiscordRole() }.map { Pair(it.discordRoleId, it) }.toMap()
    
        @JvmField
        val DONATOR_ROLES = listOf(SAPPHIRE_MEMBER, EMERALD_MEMBER, RUBY_MEMBER, DIAMOND_MEMBER, DRAGONSTONE_MEMBER, ONYX_MEMBER, ZENYTE_MEMBER)
    }
    
}
