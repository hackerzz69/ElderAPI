package com.zenyte.api.model

import java.util.*

/**
 * @author Corey
 * @since 03/10/2020
 */

data class Calendar(val page: Int,
                    val perPage: Int,
                    val totalResults: Int,
                    val results: List<Event>)

data class Event(val id: Int,
                 val title: String,
                 val start: Date,
                 val end: Date?,
                 val author: EventAuthor,
                 val postedDate: Date,
                 val description: String,
                 val url: String)

data class EventAuthor(val id: Int, val name: String)