package com.dev.news.newsportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI documentation for the API.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI newsPortalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("News Portal API")
                        .description("API for managing news, users, and comments")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("News Portal Team")
                                .email("support@newsportal.com")
                                .url("https://newsportal.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}