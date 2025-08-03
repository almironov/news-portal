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
public class NewsResponseDto {
    
    private Long id;
    private String title;
    private String text;
    private String imageUrl;
    private LocalDateTime creationDate;
    private UserSummaryDto author;
    private long commentCount;
}