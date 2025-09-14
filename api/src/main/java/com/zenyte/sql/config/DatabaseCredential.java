package com.zenyte.sql.config;

public enum DatabaseCredential {
    
    LOCAL("localhost", "elderapi_user", "Password2013!@"),
    BETA_DOCKER("localhost", "elderapi_user", "Password2013!@"),
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
