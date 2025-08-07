package com.dev.news.newsportal.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the News Portal application.
 * Groups related configuration properties with validation support.
 */
@ConfigurationProperties(prefix = "news-portal")
@Validated
@Data
public class NewsPortalProperties {

    @NestedConfigurationProperty
    @Valid
    private Database database = new Database();

    @NestedConfigurationProperty
    @Valid
    private Security security = new Security();

    /**
     * Database-related configuration properties.
     */
    @Data
    public static class Database {
        /**
         * Maximum number of database connections in the pool.
         * Must be at least 1.
         */
        @Min(1)
        private int maxConnections = 20;
    }

    /**
     * Security-related configuration properties.
     */
    @Data
    public static class Security {
        /**
         * JWT secret key for token signing and verification.
         * Cannot be blank.
         */
        @NotBlank
        private String jwtSecret;
    }
}