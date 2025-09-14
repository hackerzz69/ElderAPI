package com.zenyte.api

import com.zenyte.sql.SQLResults

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

abstract class APIRequestRunnable : Runnable {

    private lateinit var result:  Pair<APIRequestResult, Exception?>

    fun setResults(result: Pair<APIRequestResult, Exception?>) {
        this.result = result
    }

    fun getResults(): Pair<APIRequestResult, Exception?> {
        if (!::result.isInitialized) {
            run()
        }
        return if (::result.isInitialized) result else throw RuntimeException("'result' was not initialised!")
    }

    abstract fun execute(): Pair<APIRequestResult, Exception?>

    override fun run() = setResults(execute())

}