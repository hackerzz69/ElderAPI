package com.zenyte.asn

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

class IPHeuristics {
    
    companion object {
        val http = OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(5000, TimeUnit.MILLISECONDS)
                .build()
    }
    
}