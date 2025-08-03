package com.dev.news.newsportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    
    @NotBlank(message = "Text is required")
    private String text;
    
    @NotBlank(message = "Author nickname is required")
    private String authorNickname;
    
    @NotNull(message = "News ID is required")
    private Long newsId;
    
    private Long parentCommentId;
}