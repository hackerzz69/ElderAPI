package com.zenyte.sql

import com.zenyte.sql.config.DatabaseCredential

abstract class SQLRunnable : Runnable {
    
    private lateinit var result: Pair<SQLResults, Exception?>
    
    fun setResults(result: Pair<SQLResults, Exception?>) {
        this.result = result
    }
    
    fun getResults(): Pair<SQLResults, Exception?> {
        if (!::result.isInitialized) {
            run()
        }
        return if (::result.isInitialized) result else throw RuntimeException("'result' was not initialised!")
    }
    
    abstract fun execute(auth: DatabaseCredential): Pair<SQLResults, Exception?>
    
    open fun prepare() = HikariPool.submit(this)
    
    override fun run() = prepare()
}
