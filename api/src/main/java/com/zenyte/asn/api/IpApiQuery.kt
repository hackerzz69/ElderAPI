package com.zenyte.asn.api

import com.google.common.base.Stopwatch
import com.google.gson.JsonParser
import com.zenyte.asn.IPHeuristics.Companion.http
import okhttp3.HttpUrl
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

class IpApiQuery {
    
    companion object {
        var REQUEST_TIMEOUT = 1000
        var requests = 0

        val auth = "I1hwmh0wKYqQyhk"

        var parser = JsonParser()
        var cycle = Stopwatch.createStarted()
    }
    
    fun execute(ip: String): IpStack.GenericIPResponse {
        checkNotNull(http) {
            "[IP-API] The HTTP client cannot be null!"
        }
        
        checkNotNull(ip) {
            "[IP-API] Your ip address cannot be empty!"
        }
        
        synchronized(cycle) {
            if (requests > 10 && cycle.elapsed(TimeUnit.SECONDS) >= 60) {
                requests = 0
                cycle.reset().reset()
                System.out.println("[IP-API] Request limit has been reset!")
            }
            
            if (++requests >= REQUEST_TIMEOUT) {
                System.out.println("[IP-API] EXCEEDED request limit! On address: $ip")
                return IpStack.GenericIPResponse("error", "")
            }
        }
        
                val request = Request.Builder()
                .url(HttpUrl.Builder()
                        .scheme("http")
                        .host("pro.ip-api.com")
                        .addPathSegment("json")
                        .addPathSegment(ip)
                        .addQueryParameter("key", auth)
                        .build())
                .header("User-Agent", "Zenyte API")
                .build()
        
        try {
            
            val response = http.newCall(request).execute()
            val body = response.body
            
            checkNotNull(body) {
                "[IP-API] The body of the response was null!"
            }
    
            val responseCode = response.code / 100
            if (responseCode == 2) { // http 200
                val json = body.string()
                
                if (json.length < 1) {
                    System.out.println("[IP-API] The json response was empty!")
                    return IpStack.GenericIPResponse("error", "")
                }
                
                var element = parser.parse(json).asJsonObject
                val asn = element.get("as").asString
                var country = element.get("countryCode").asString

                if(country == null || country.isEmpty())
                    country = "N/A"

                if (asn.isEmpty() || asn == null)
                    return IpStack.GenericIPResponse("bad-asn", country)

                if (!asn.startsWith("AS")) {
                    System.out.println("[IP-API] Unrecognized asn format: " + asn)
                    return IpStack.GenericIPResponse("bad-asn", country)
                }
                
                return IpStack.GenericIPResponse(asn.substring(2, asn.indexOf(' ')), country)
            }
            
            
        } catch (e: Exception) {
            e.printStackTrace()
            System.out.println("[IP-API] An exception has been caught and the query failed!")
            return IpStack.GenericIPResponse("error", "")
        }
        
        return IpStack.GenericIPResponse("error", "")
    }

}