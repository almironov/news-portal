package com.dev.news.newsportal.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Event record representing a news update event.
 * Contains all necessary information about the updated news article.
 */
public record NewsUpdatedEvent(
        @NotNull
        Long newsId,
        
        @NotBlank
        String title,
        
        @NotBlank
        String text,
        
        String imageUrl,
        
        @NotNull
        LocalDateTime creationDate,
        
        LocalDateTime updateDate,
        
        @NotNull
        Long authorId,
        
        @NotBlank
        String authorNickname,
        
        @NotNull
        LocalDateTime eventTimestamp
) {
    @Override
    public String toString() {
        return String.format("NewsUpdatedEvent{newsId=%d, title='%s', authorId=%d, authorNickname='%s', updateDate=%s, eventTimestamp=%s}",
                newsId, title, authorId, authorNickname, updateDate, eventTimestamp);
    }
}