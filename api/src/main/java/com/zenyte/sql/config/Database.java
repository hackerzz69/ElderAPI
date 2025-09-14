package com.zenyte.sql.config;

import java.util.HashMap;

public class Database {
    
    public static HashMap<String, Database> databases = new HashMap<>();
    
    private DatabaseDetails details;
    
    public Database(final DatabaseDetails details) {
        this.details = details;
    }
    
    public DatabaseDetails getDetails() {
        return details;
    }
}
