package com.zenyte.sql.query

import com.zenyte.asn.api.AntiknoxQuery
import com.zenyte.asn.api.IpApiQuery
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

class IpVerificationQuery(private val ip: String) : SQLRunnable() {
    
    companion object {
        val COMPARE_IP = "SELECT * FROM iptables_all WHERE ipAddress = ?"
        val INSERT_IP = "INSERT INTO iptables_all ( ipAddress, valid ) VALUES ( ?, ? )"
        val COMPARE_ASN = "SELECT COUNT(*) as total FROM asn_blacklist WHERE asn = ?"

        val CHECK_QUERY = "SELECT * FROM antiknox_cache WHERE ipAddress = ?"
        val CACHE_IP = "INSERT INTO antiknox_cache (ipAddress, messageDigest, legitimate) VALUES (?, ?, ?)"

        val countries = listOf("CN", "HK")
        
        val VALID_RESULT = IpVerificationResult(null, true) to null
        val INVALID_RESULT = IpVerificationResult(null, false) to null
    }
    
    override fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?> {
        try {
            HikariPool.getConnection(auth, "zenyte_main").use { con ->
                con.prepareStatement(COMPARE_IP).use { pst ->
                    pst.setString(1, ip)
                    pst.execute()
    
                    if (pst.resultSet.next())
                        return if (pst.resultSet.getInt("valid") == 1) VALID_RESULT else INVALID_RESULT
                    else {
                        con.prepareStatement(COMPARE_ASN).use { chk ->
                            val data = IpApiQuery().execute(ip)
    
                            chk.setInt(1, if (data.asn == "error") 1 else data.asn.toInt())
                            chk.execute()
    
                            var success = !countries.contains(data.country)
    
                            if (chk.resultSet.next())
                                success = chk.resultSet.getInt("total") == 0 && !countries.contains(data.country)

                            con.prepareStatement(CHECK_QUERY).use {
                                it.setString(1, ip)
                                it.execute()

                                if(it.resultSet.next())
                                    success = it.resultSet.getBoolean("legitimate")
                                else {
                                    var response = AntiknoxQuery().execute(ip)

                                    con.prepareStatement(CACHE_IP).use { stmt ->
                                        stmt.setString(1, ip)
                                        stmt.setString(2, response.digest)
                                        stmt.setBoolean(3, response.legit)
                                        stmt.execute()

                                        success = response.legit
                                    }
                                }
                            }

                            /** Insert the users ip w/ valid/invalid result */
                            con.prepareStatement(INSERT_IP).use { ins ->
                                ins.setString(1, ip)
                                ins.setInt(2, if (success) 1 else 0)
                                ins.execute()
    
                                return if (success) VALID_RESULT else INVALID_RESULT
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return INVALID_RESULT
        }
    }
    
    data class IpVerificationResult(override val results: ResultSet?, val success: Boolean) : SQLResults
}