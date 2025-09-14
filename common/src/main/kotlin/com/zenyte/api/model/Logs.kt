package com.zenyte.api.model

import java.sql.Timestamp

/**
 * @author Corey
 * @since 10:59 - 21/07/2019
 */

data class PunishmentLog(val modUserId: Int,
                         val modName: String,
                         val offenderUserId: Int,
                         val offender: String,
                         val ipAddress: String,
                         val macAddress: String,
                         val actionType: String,
                         val expires: String,
                         val reason: String)

data class TradeLog @JvmOverloads constructor(val id: Int? = null,
                                              val user: String,
                                              val userIp: String,
                                              val given: List<TradedItem>,
                                              val partner: String,
                                              val partnerIp: String,
                                              val received: List<TradedItem>,
                                              val world: Int,
                                              val timeAdded: Timestamp? = null)

data class TradedItem(val id: Int, val amount: Int, val name: String)
