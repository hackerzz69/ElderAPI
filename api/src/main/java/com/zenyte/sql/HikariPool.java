package com.zenyte.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zenyte.sql.config.Database;
import com.zenyte.sql.config.DatabaseCredential;
import com.zenyte.sql.config.DatabaseDetails;
import com.zenyte.sql.config.DatabaseTopology;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class HikariPool {
    
    public final static boolean DEBUG = true;
    
    public final static HashMap<DatabaseCredential, HashMap<String, HikariDataSource>> pools = new HashMap<>();
    public final static DatabaseTopology TOPOLOGY = DatabaseTopology.BETA;
    
    public final int MINIMUM_DATABASE_CONNECTIONS = 1;
    public final int MAXIMUM_DATABASE_CONNECTIONS = 4;
    
    public HikariPool() {
        for (DatabaseCredential auth : TOPOLOGY.getNodes())
            pools.put(auth, new HashMap<>());
        
        for (Database database : Database.databases.values()) {
            final HikariConfig config = new HikariConfig();
            final DatabaseCredential auth = database.getDetails().getAuth();
            config.setPoolName(database.getDetails().getDatabase());
            config.setUsername(auth.getUser());
            config.setPassword(auth.getPass());
            config.setJdbcUrl("jdbc:mysql://" + auth.getHost() + ":3306/" + database.getDetails().getDatabase()
                                      + "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
    
            config.setMinimumIdle(MINIMUM_DATABASE_CONNECTIONS);
            config.setMaximumPoolSize(MAXIMUM_DATABASE_CONNECTIONS);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            config.addDataSourceProperty("prepStmtCacheSize", 256);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            pools.get(database.getDetails().getAuth()).put(database.getDetails().getDatabase(), new HikariDataSource(config));
        }
    }
    
    public static Connection getConnection(final DatabaseCredential auth, final String database) throws SQLException {
        return getPool(auth, database).getConnection();
    }
    
    public static HikariDataSource getPool(final DatabaseCredential auth, final String name) {
        return pools.get(auth).get(name);
    }
    
    public static void submit(SQLRunnable query) {
        long start = 0, end = 0;
    
        if (query == null)
            return;
    
        if (DEBUG)
            start = System.currentTimeMillis();
    
        for (DatabaseCredential auth : TOPOLOGY.getNodes()) {
            try {
                query.setResults(query.execute(auth));
                
                if (DEBUG) {
                    end = System.currentTimeMillis();
                    System.out.println("Query [" + query.getClass().getSimpleName() + "] took approximately " + (end - start) + "ms to execute.");
                }
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void preload() {
        external:
        for (final DatabaseDetails entry : DatabaseDetails.VALUES) {
            internal:
            for (final DatabaseCredential auth : TOPOLOGY.getNodes()) {
                if (auth != entry.getAuth())
                    continue external;
                
                if (entry.getDatabase() != null)
                    Database.databases.put(entry.getDatabase(), new Database(entry));
            }
        }
    }
}
