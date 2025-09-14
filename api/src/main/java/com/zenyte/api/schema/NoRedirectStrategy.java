package com.zenyte.api.schema;

import org.springframework.security.web.RedirectStrategy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * No-op redirect strategy for REST APIs (no browser redirects).
 *
 * @author Noele
 */
public class NoRedirectStrategy implements RedirectStrategy {

    @Override
    public void sendRedirect(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final String url
    ) throws IOException {
        // Intentionally left blank: no redirect is required in a pure REST API
    }
}
