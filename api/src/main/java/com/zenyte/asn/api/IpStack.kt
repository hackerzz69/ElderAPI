package com.zenyte.asn.api

import com.google.gson.JsonParser
import com.zenyte.asn.IPHeuristics
import okhttp3.HttpUrl
import okhttp3.Request

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class IpStack {

    companion object {
        val auth = "c03e77d526c5569923da1c0e61587c19"
        var parser = JsonParser()
    }

    fun execute(ip: String): GenericIPResponse {
        checkNotNull(IPHeuristics.http) {
            "[IPSTACK] The HTTP client cannot be null!"
        }

        checkNotNull(ip) {
            "[IPSTACK] Your ip address cannot be empty!"
        }

        val request = Request.Builder()
                .url(HttpUrl.Builder()
                        .scheme("http")
                        .host("api.ipstack.com")
                        .addPathSegment(ip)
                        .addQueryParameter("access_key", auth)
                        .build())
                .header("User-Agent", "Zenyte API")
                .build()
        try {

            val response = IPHeuristics.http.newCall(request).execute()
            val body = response.body

            checkNotNull(body) {
                "[IP-API] The body of the response was null!"
            }
    
            val responseCode = response.code / 100
            if(responseCode == 2) {
                val json = body.string()
                if (json.length < 1) {
                    System.out.println("[IPSTACK] The json response was empty!")
                    return GenericIPResponse("error", "error")
                }

                val element = parser.parse(json).asJsonObject
                val selectCon = element.get("connection").toString()

                if(selectCon != "null" && selectCon != null) {
                    val conJson = parser.parse(json).asJsonObject
                    return GenericIPResponse(conJson.get("asn").asString, conJson.get("country_code").asString)
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            System.out.println("[IPSTACK] An exception has been caught and the query failed!")
            return GenericIPResponse("error", "[IPSTACK] An exception has been caught and the query failed!")
        }

        return GenericIPResponse("error", "[IPSTACK] No result in processing!")
    }

    data class GenericIPResponse(val asn: String, val country: String)

}