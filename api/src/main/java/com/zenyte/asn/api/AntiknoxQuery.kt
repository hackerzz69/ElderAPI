package com.zenyte.asn.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.zenyte.asn.IPHeuristics.Companion.http
import okhttp3.HttpUrl
import okhttp3.Request

class AntiknoxQuery {

    companion object {
        private const val AUTH =
            "3b3ded7c82983e4fb66b1628434573dd83055c4f80acafd2fc088d3b3c2cabdb"
    }

    fun execute(ip: String): AntiknoxAPIResult {
        requireNotNull(http) { "[ANTIKNOX] The HTTP client cannot be null!" }
        require(ip.isNotBlank()) { "[ANTIKNOX] IP address cannot be empty!" }

        val url = HttpUrl.Builder()
            .scheme("http")
            .host("api.antiknox.net")
            .addPathSegment("lookup")
            .addPathSegment(ip)
            .addQueryParameter("auth", AUTH)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Zenyte API")
            .build()

        return try {
            http!!.newCall(request).execute().use { response ->
                val body = response.body ?: return AntiknoxAPIResult(false, "")

                if (!response.isSuccessful) {
                    println("[ANTIKNOX] Non-200 response: ${response.code}")
                    return AntiknoxAPIResult(false, "")
                }

                val json = body.string()
                if (json.isBlank()) {
                    println("[ANTIKNOX] Empty JSON response")
                    return AntiknoxAPIResult(false, "")
                }

                val element: JsonObject = JsonParser.parseString(json).asJsonObject

                element.getAsJsonObject("direct")?.let { direct ->
                    val type = direct.get("type").asString
                    return AntiknoxAPIResult(type != "tor" && type != "proxy", json)
                }

                element.getAsJsonObject("heuristics")?.let { heuristics ->
                    val label = heuristics.get("label").asString
                    return AntiknoxAPIResult(label != "hosting", json)
                }

                AntiknoxAPIResult(false, json)
            }
        } catch (e: Exception) {
            println("[ANTIKNOX] Exception during query: ${e.message}")
            e.printStackTrace()
            AntiknoxAPIResult(false, "")
        }
    }

    data class AntiknoxAPIResult(val legit: Boolean, val digest: String)
}
