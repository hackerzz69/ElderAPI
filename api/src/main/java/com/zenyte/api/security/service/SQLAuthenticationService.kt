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
        println("🔎 [SQLAuthService] findByToken called with token='$token' ip='$ip'")
        val result = if (checkToken(token, ip)) {
            println("✅ [SQLAuthService] Token '$token' is valid")
            ApiUserBuilder().token(token).build()
        } else {
            println("❌ [SQLAuthService] Token '$token' is invalid")
            null
        }
        return Optional.ofNullable(result)
    }

    private fun checkToken(token: String, ip: String): Boolean {
        if (cache.containsKey(token)) {
            val cached = cache[token] ?: false
            println("💾 [SQLAuthService] Cache hit for token='$token' -> $cached")
            return cached
        }

        println("🗄️ [SQLAuthService] Cache miss for token='$token'. Querying database…")
        val result = (ApiAuthenticationQuery(token, ip).getResults().first as ApiAuthenticationResult).successful
        println("📊 [SQLAuthService] Database returned successful=$result for token='$token'")
        cache[token] = result
        return result
    }
}
