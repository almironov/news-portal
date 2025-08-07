package com.dev.news.newsportal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport
public class WebConfig {
    // Spring Data Web support is automatically configured in Spring Boot
    // This annotation ensures Pageable parameters are properly resolved
}