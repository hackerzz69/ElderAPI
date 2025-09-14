package com.zenyte.api.security.token;

import com.zenyte.util.ApiUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Custom filter for token-based authentication.
 *
 * Extracts a Bearer token (from Authorization header or "t" parameter),
 * pairs it with the client IP, and passes it to the AuthenticationManager.
 *
 * @author Noele
 */
public final class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER = "Bearer";

    // ✅ No-arg constructor – always process requests
    public TokenAuthenticationFilter() {
        super("/**"); // matches everything; actual access rules are handled by HttpSecurity DSL
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        final String param = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .orElse(request.getParameter("t"));

        final String cloudflare = request.getHeader("CF-Connecting-IP");
        final String ip = (cloudflare == null) ? request.getRemoteAddr() : cloudflare;

        String token = Optional.ofNullable(param)
                .map(value -> ApiUtils.removeStart(value, BEARER))
                .map(String::trim)
                .orElseThrow(() -> new BadCredentialsException("Missing authentication token."));

        Authentication authentication = new UsernamePasswordAuthenticationToken(token, ip);
        return getAuthenticationManager().authenticate(authentication);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
