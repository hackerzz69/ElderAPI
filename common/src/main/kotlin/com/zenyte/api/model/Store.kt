package com.zenyte.api.model

import java.util.*

data class StorePurchase(val itemName: String,
                         val id: Int,
                         val amount: Int,
                         val itemQuantity: Int,
                         val price: Double,
                         val discount: Double)

enum class Bond(val id: Int, val credits: Int, val amount: Int) {
    
    GREY(30051, 50, 5),
    GREEN(13190, 100, 10),
    BLUE(30017, 500, 50),
    RED(30018, 1000, 100);
    
    companion object {
        
        private val values = values()
        private val bonds = HashMap<Int, Bond>(values.size)
        
        @JvmStatic
        val itemIds = values.map { it.id }
        
        init {
            for (bond in values) {
                bonds[bond.id] = bond
            }
        }
        
        fun getBond(id: Int): Bond {
            return bonds.getValue(id)
        }
    }
    
}