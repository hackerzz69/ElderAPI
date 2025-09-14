package com.zenyte.api.security;

import com.zenyte.api.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Completely disables security in development.
 *
 * @author Corey
 * @since 17/05/19
 */
@Configuration
@Profile(Profiles.DEVELOPMENT)
public class DevelopmentSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
