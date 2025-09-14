package com.zenyte.api.model

/**
 * @author Corey
 * @since 12/03/2020
 */

data class GrandExchangeItemPriceInstant(
        val seconds: Long,
        val nanos: Int
)

data class GrandExchangeItemPrice(
        val id: Int,
        val name: String,
        val price: Int,
        val time: GrandExchangeItemPriceInstant
)