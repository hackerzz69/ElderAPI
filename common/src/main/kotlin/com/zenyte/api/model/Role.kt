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

    // ─── Core Staff ────────────────────────────────
    OWNER(12, 1389074871467774055),
    ADMIN(4, 1389075141798920203),
    DEVELOPER(11, 1393627067332952135),
    STAFF(discordRoleId = 1393627067332952135),
    MODERATOR(6, 1416834078522474657),
    SENIOR_MODERATOR(20, 1416834295124725791),
    SUPPORT(8, 1416834532539236463),
    FORUM_MODERATOR(7, 1416834827075850381),
    WEB_DEVELOPER(19, 1416835086262861958),

    // ─── Donator Tiers ─────────────────────────────
    DONATOR_SAPPHIRE(14, 1416835376797978704),
    DONATOR_EMERALD(15, 1416835869242818600),
    DONATOR_RUBY(16, 1416835975241400370),
    DONATOR_DIAMOND(17, 1416836115356319859),
    DONATOR_DRAGONSTONE(21, 1416836310374809772),
    DONATOR_ONYX(22, 1416836524011425822),
    DONATOR_ZENYTE(23, 1416836749187088594),

    // ─── Special Roles ─────────────────────────────
    VERIFIED(discordRoleId = 1416837519294595174),
    CONTENT_CREATOR(18, 1416837667584479383), // replaces YOUTUBER
    BETA_TESTER(discordRoleId = 1416837833280192532), // replaces BETA_CREW
    BETA_MEDAL(discordRoleId = 1416837925009887393),
    BOT(discordRoleId = 1416837997243928727), // replaces ZENYTE_BOT

    // ─── Default Registered Member ────────────────
    REGISTERED_MEMBER(forumGroupId = 3);

    fun isDiscordRole() = discordRoleId != -1L
    fun isForumRole() = forumGroupId != -1

    companion object {
        @JvmField
        val VALUES = Role.entries

        @JvmField
        val FORUM_GROUPS = VALUES.filter { it.isForumRole() }.associateBy { it.forumGroupId }

        @JvmField
        val DISCORD_ROLES = VALUES.filter { it.isDiscordRole() }.associateBy { it.discordRoleId }

        @JvmField
        val DONATOR_ROLES = listOf(
            DONATOR_SAPPHIRE,
            DONATOR_EMERALD,
            DONATOR_RUBY,
            DONATOR_DIAMOND,
            DONATOR_DRAGONSTONE,
            DONATOR_ONYX,
            DONATOR_ZENYTE
        )
    }
}
