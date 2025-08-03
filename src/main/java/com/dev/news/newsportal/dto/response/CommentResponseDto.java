package com.dev.news.newsportal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    
    private Long id;
    private String text;
    private LocalDateTime creationDate;
    private String authorNickname;
    
    @Builder.Default
    private List<CommentListItemDto> replies = new ArrayList<>();
}