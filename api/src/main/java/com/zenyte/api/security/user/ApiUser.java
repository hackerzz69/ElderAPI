package com.zenyte.api.security.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */

public class ApiUser implements UserDetails {
    
    private final String token;
    private final boolean enabled;
    
    @JsonCreator
    ApiUser(@JsonProperty("token") String token,
            @JsonProperty("enabled") boolean enabled) {
        this.token = Objects.requireNonNull(token);
        this.enabled = enabled;
    }
    
    public static ApiUserBuilder builder() {
        return new ApiUserBuilder();
    }
    
    @JsonIgnore
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    public String getToken() {
        return token;
    }
    
    @Override
    public String getUsername() {
        return null;
    }
    
    @JsonIgnore
    @Override
    public String getPassword() {
        return null;
    }
    
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
}
