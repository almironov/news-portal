package com.dev.news.newsportal.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Event record representing a comment creation event.
 * Contains all necessary information about the created comment.
 */
public record CommentCreatedEvent(
        @NotNull
        Long commentId,
        
        @NotBlank
        String text,
        
        @NotNull
        LocalDateTime creationDate,
        
        @NotBlank
        String authorNickname,
        
        @NotNull
        Long newsId,
        
        @NotBlank
        String newsTitle,
        
        Long parentCommentId,
        
        @NotNull
        LocalDateTime eventTimestamp
) {
    @Override
    public String toString() {
        return String.format("CommentCreatedEvent{commentId=%d, newsId=%d, newsTitle='%s', authorNickname='%s', parentCommentId=%s, eventTimestamp=%s}",
                commentId, newsId, newsTitle, authorNickname, parentCommentId, eventTimestamp);
    }
}