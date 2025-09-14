package com.zenyte.api.security.user;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
public class ApiUserBuilder {
    private String token;
    private String username;
    private String password;
    private boolean enabled;
    
    public ApiUserBuilder token(String token) {
        this.token = token;
        return this;
    }
    
    public ApiUserBuilder username(String username) {
        this.username = username;
        return this;
    }
    
    public ApiUserBuilder password(String password) {
        this.password = password;
        return this;
    }
    
    public ApiUserBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public ApiUser build() {
        return new ApiUser(token, true);
    }
}
