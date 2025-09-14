package com.zenyte.api;

import org.springframework.context.annotation.Profile;

/**
 * Represents {@link Profile} configurations.
 * <p>
 * You can configure the used profile by defining {@code spring.profiles.active=production}
 * in the "application.properties" file or by setting JVM option {@code -Dspring.profiles.active=production}
 * </p>
 */
public final class Profiles {
    
    /**
     * The production profile.
     */
    public static final String PRODUCTION = "production";
    
    /**
     * The development profile.
     */
    public static final String DEVELOPMENT = "development";
    
    /**
     * Suppress default constructor to discourage instantiation.
     */
    private Profiles() {
    }
    
}
