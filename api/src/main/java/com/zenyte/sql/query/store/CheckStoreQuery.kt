package com.zenyte.sql.query.store

import com.zenyte.api.model.StorePurchase
import com.zenyte.sql.HikariPool
import com.zenyte.sql.SQLResults
import com.zenyte.sql.SQLRunnable
import com.zenyte.sql.config.DatabaseCredential
import java.sql.ResultSet

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class CheckStoreQuery(val displayName: String): SQLRunnable() {

    val CHECK_QUERY = "SELECT * FROM store_purchases WHERE claimed = 0 AND username = ?"
    val CLAIM_QUERY = "UPDATE store_purchases SET claimed = 1 WHERE username = ? and claimed = 0"

    val EMPTY_RESULT = DonationResult(null, null) to null

    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "elder_main").use { con ->
                con.prepareStatement(CHECK_QUERY).use { chk ->
                    chk.setString(1, displayName)
                    chk.execute()
    
                    val list = ArrayList<StorePurchase>()

                    while (chk.resultSet.next()) {
                        val name = chk.resultSet.getString(chk.resultSet.findColumn("item_name"))
                        val id = chk.resultSet.getInt(chk.resultSet.findColumn("item_id"))
                        val amount = chk.resultSet.getInt(chk.resultSet.findColumn("quantity"))
                        val itemQuantity = chk.resultSet.getInt(chk.resultSet.findColumn("item_amount"))
                        val price = chk.resultSet.getDouble(chk.resultSet.findColumn("price"))
                        val discount = chk.resultSet.getDouble(chk.resultSet.findColumn("discount"))

                        if (id == 0 || amount == 0)
                            continue
    
                        list.add(StorePurchase(name, id, amount, itemQuantity, price, discount))
                    }

                    con.prepareStatement(CLAIM_QUERY).use { claim ->
                        claim.setString(1, displayName)
                        claim.execute()
                    }

                    return DonationResult(chk.resultSet, list) to null
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            return EMPTY_RESULT
        }
    }
    
    data class DonationResult(override val results: ResultSet?, val items: List<StorePurchase>?) : SQLResults


}