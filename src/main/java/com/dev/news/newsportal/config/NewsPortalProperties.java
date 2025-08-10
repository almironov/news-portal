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

    @NestedConfigurationProperty
    @Valid
    private RabbitMq rabbitMq = new RabbitMq();

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

    /**
     * RabbitMQ-related configuration properties.
     * Simplified configuration for message publishing only.
     */
    @Data
    public static class RabbitMq {
        @NestedConfigurationProperty
        @Valid
        private Exchanges exchanges = new Exchanges();

        @Data
        public static class Exchanges {
            @NestedConfigurationProperty
            @Valid
            private NewsExchange news = new NewsExchange();

            @NestedConfigurationProperty
            @Valid
            private CommentsExchange comments = new CommentsExchange();

            @Data
            public static class NewsExchange {
                @NotBlank
                private String exchange = "exchange.news";

                @NestedConfigurationProperty
                @Valid
                private Binding binding = new Binding();

                @Data
                public static class Binding {
                    @NestedConfigurationProperty
                    @Valid
                    private Created created = new Created();

                    @NestedConfigurationProperty
                    @Valid
                    private Updated updated = new Updated();

                    @Data
                    public static class Created {
                        @NotBlank
                        private String queue = "queue.news.created";
                        @NotBlank
                        private String key = "news.created";
                    }

                    @Data
                    public static class Updated {
                        @NotBlank
                        private String queue = "queue.news.updated";
                        @NotBlank
                        private String key = "news.updated";
                    }
                }
            }

            @Data
            public static class CommentsExchange {
                @NotBlank
                private String exchange = "exchange.comments";

                @NestedConfigurationProperty
                @Valid
                private Binding binding = new Binding();

                @Data
                public static class Binding {
                    @NestedConfigurationProperty
                    @Valid
                    private Created created = new Created();

                    @Data
                    public static class Created {
                        @NotBlank
                        private String queue = "queue.comments.created";
                        @NotBlank
                        private String key = "comments.created";
                    }
                }
            }
        }
    }
}