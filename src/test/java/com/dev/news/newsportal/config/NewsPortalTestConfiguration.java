package com.dev.news.newsportal.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Test configuration class for News Portal application.
 * Provides common test beans and configuration for test environment.
 */
@TestConfiguration
public class NewsPortalTestConfiguration {

    /**
     * Provides a fixed clock for consistent test results.
     * This ensures that time-dependent tests are deterministic.
     */
    @Bean
    @Primary
    @Profile("test")
    public Clock testClock() {
        // Fixed time for consistent test results: 2024-01-01T12:00:00Z
        return Clock.fixed(Instant.parse("2025-01-01T12:00:00Z"), ZoneId.of("UTC"));
    }
}