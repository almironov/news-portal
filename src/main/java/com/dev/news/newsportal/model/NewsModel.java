package com.dev.news.newsportal.model;

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
public class NewsModel {

    private Long id;
    private String title;
    private String text;
    private String imageUrl;
    private LocalDateTime creationDate;
    private UserModel author;

    @Builder.Default
    private List<CommentModel> comments = new ArrayList<>();
}