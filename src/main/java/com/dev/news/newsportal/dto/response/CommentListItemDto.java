package com.dev.news.newsportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListItemDto {
    
    private Long id;
    private String text;
    private LocalDateTime creationDate;
    private String authorNickname;
    private boolean hasReplies;
}