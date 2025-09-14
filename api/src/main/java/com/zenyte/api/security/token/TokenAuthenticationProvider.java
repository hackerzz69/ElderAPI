package com.zenyte.api.security.token;

import com.zenyte.api.security.service.SQLAuthenticationService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
@Component
public final class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    
    @NonNull
    private final SQLAuthenticationService service;
    
    public TokenAuthenticationProvider(SQLAuthenticationService service) {
        this.service = Objects.requireNonNull(service);
    }
    
    @Override
    public void additionalAuthenticationChecks(UserDetails details, UsernamePasswordAuthenticationToken token) {
        if (!details.isEnabled()) {
            System.out.println("Credentials are not enabled.");
            throw new BadCredentialsException("Credentials are not enabled.");
        }
    }
    
    @Override
    public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken token) {
        return service.findByToken(token.getPrincipal().toString(), token.getCredentials().toString()).
                                                                                                              orElseThrow(() -> new UsernameNotFoundException("Unable to find user with associated by token:" + token));
    }
}