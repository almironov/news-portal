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
public class CommentModel {

    private Long id;
    private String text;
    private LocalDateTime creationDate;
    private String authorNickname;
    private Long newsId;
    private Long parentCommentId;

    @Builder.Default
    private List<CommentModel> replies = new ArrayList<>();
}