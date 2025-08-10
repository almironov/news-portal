package com.dev.news.newsportal.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Event record representing a news creation event.
 * Contains all necessary information about the created news article.
 */
public record NewsCreatedEvent(
        @NotNull
        Long newsId,
        
        @NotBlank
        String title,
        
        @NotBlank
        String text,
        
        String imageUrl,
        
        @NotNull
        LocalDateTime creationDate,
        
        @NotNull
        Long authorId,
        
        @NotBlank
        String authorNickname,
        
        @NotNull
        LocalDateTime eventTimestamp
) {
    @Override
    public String toString() {
        return String.format("NewsCreatedEvent{newsId=%d, title='%s', authorId=%d, authorNickname='%s', eventTimestamp=%s}",
                newsId, title, authorId, authorNickname, eventTimestamp);
    }
}