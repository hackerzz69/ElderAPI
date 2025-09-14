package com.zenyte.sql;

public class SQLThread extends Thread {
    
    public static HikariPool pool;
    public static volatile boolean ENABLED = true;
    
    @Override
    public void run() {
        try {
    
            HikariPool.preload();
            pool = new HikariPool();
    
            while (ENABLED) {
                QueryExecutor.process();
                sleep(250);
            }
    
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
}
