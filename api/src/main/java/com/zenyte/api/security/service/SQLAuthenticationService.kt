package com.zenyte.api.security.service

import com.zenyte.api.security.user.ApiUser
import com.zenyte.api.security.user.ApiUserBuilder
import com.zenyte.sql.query.ApiAuthenticationQuery
import com.zenyte.sql.query.ApiAuthenticationQuery.ApiAuthenticationResult
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
@Service
class SQLAuthenticationService {
    
    private val cache = hashMapOf<String, Boolean>()
    
    fun findByToken(token: String, ip: String): Optional<ApiUser> {
        return Optional.ofNullable(if (checkToken(token, ip)) ApiUserBuilder().token(token).build() else null)
    }
    
    private fun checkToken(token: String, ip: String): Boolean {
        when (cache.contains(token)) {
            true -> return cache.get(token) ?: false
            false -> {
                val success = (ApiAuthenticationQuery(token, ip).getResults().first as ApiAuthenticationResult).successful
                cache.put(token, success)
                return success
            }
        }
    }
}
