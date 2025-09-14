package com.zenyte.api.`in`

import com.zenyte.api.model.GrandExchangeItemPrice
import com.zenyte.common.datastore.RedisCache
import com.zenyte.common.CommonConfig.gson
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author Corey
 * @since 30/04/19
 */
@RestController
@RequestMapping("/runelite")
object RuneLiteController {
    
    private val logger = KotlinLogging.logger {}
    
    private const val RUNELITE_ITEM_PRICES_KEY = "runelite:items:prices"
    private const val RUNELITE_INDIVIDUAL_ITEM_PRICE_KEY_TEMPLATE = "runelite:items:prices:%d"
    
    private val priceLock = ReentrantReadWriteLock()
    
    @GetMapping("/items/prices/{itemId}", produces = ["application/json"])
    fun getItemPrice(@PathVariable itemId: Int): String? {
        return priceLock.read {
            RedisCache.redis.sync().get(RUNELITE_INDIVIDUAL_ITEM_PRICE_KEY_TEMPLATE.format(itemId))
        }
    }
    
    @GetMapping("/items/prices", produces = ["application/json"])
    fun getItemPrices(): String? {
        return priceLock.read {
            RedisCache.redis.sync().get(RUNELITE_ITEM_PRICES_KEY)
        }
    }
    
    @PostMapping("/items/prices")
    fun updateItemPrices(@RequestBody prices: List<GrandExchangeItemPrice>) {
        logger.info { "Updating ${prices.size} prices" }
        priceLock.write {
            RedisCache.redis.sync().set(RUNELITE_ITEM_PRICES_KEY, gson.toJson(prices))
            prices.forEach {
                RedisCache.redis.sync().set(RUNELITE_INDIVIDUAL_ITEM_PRICE_KEY_TEMPLATE.format(it.id), gson.toJson(it))
            }
        }
        logger.info { "Successfully updated item prices" }
    }
    
}
