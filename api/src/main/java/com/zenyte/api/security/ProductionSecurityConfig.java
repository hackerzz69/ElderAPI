package com.zenyte.api.security;

import com.zenyte.api.Profiles;
import com.zenyte.api.schema.NoRedirectStrategy;
import com.zenyte.api.security.token.TokenAuthenticationFilter;
import com.zenyte.api.security.token.TokenAuthenticationProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.util.List;
import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile(Profiles.PRODUCTION)
public class ProductionSecurityConfig {

    private final TokenAuthenticationProvider provider;

    public ProductionSecurityConfig(TokenAuthenticationProvider provider) {
        this.provider = Objects.requireNonNull(provider);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(provider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless sessions
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Exception handling: return 403 on protected endpoints if unauthenticated
                .exceptionHandling(ex -> ex.authenticationEntryPoint(forbiddenEntryPoint()))

                // Authentication
                .authenticationManager(authenticationManager())
                .addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/hiscores/**",
                                "/worldinfo/**",
                                "/user/info/*",
                                "/user/awards/*",
                                "/user/adv/*",
                                "/runelite/items/**",
                                "/public/**",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Disable unused features
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter restAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(); // update ctor if needed
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler());
        return filter;
    }

    @Bean
    public SimpleUrlAuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
        successHandler.setRedirectStrategy(new NoRedirectStrategy());
        return successHandler;
    }

    @Bean
    public FilterRegistrationBean<TokenAuthenticationFilter> disableAutoRegistration(TokenAuthenticationFilter filter) {
        FilterRegistrationBean<TokenAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationEntryPoint forbiddenEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }
}
