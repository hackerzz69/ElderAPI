package com.zenyte.sql.config;

public enum DatabaseDetails {
    
    MAIN(DatabaseCredential.LOCAL, "elder_main"),
    FORUM(DatabaseCredential.LOCAL, "elder_forum"),
    
    BETA_MAIN(DatabaseCredential.BETA_DOCKER, "elder_main"),
    BETA_FORUM(DatabaseCredential.BETA_DOCKER, "elder_forum"),
    ;
    
    public static final DatabaseDetails[] VALUES = values();
    private DatabaseCredential auth;
    private String database;
    
    DatabaseDetails(final DatabaseCredential auth, final String database) {
        this.auth = auth;
        this.database = database;
    }
    
    public DatabaseCredential getAuth() {
        return auth;
    }
    
    public String getDatabase() {
        return database;
    }
}
