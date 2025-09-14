package com.zenyte.sql.config;

public enum DatabaseCredential {
    
    LOCAL("localhost", "elder", "m@iGJiEyxEQ0]2TW"),
    BETA_DOCKER("localhost", "elder", "m@iGJiEyxEQ0]2TW"),
    ;
    
    private String host;
    private String user;
    private String pass;
    
    DatabaseCredential(final String host, final String user, final String pass) {
        this.host = host;
        this.user = user;
        this.pass = pass;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getPass() {
        return pass;
    }
}
