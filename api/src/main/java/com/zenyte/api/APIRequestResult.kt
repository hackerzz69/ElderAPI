package com.zenyte.api

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
interface APIRequestResult {
    val result: Boolean?
}

data class NoneResult(override val result: Boolean?) : APIRequestResult