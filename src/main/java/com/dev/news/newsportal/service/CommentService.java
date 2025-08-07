package com.dev.news.newsportal.service;

import com.dev.news.newsportal.model.CommentModel;

import java.util.List;

public interface CommentService {

    CommentModel findById(Long id);

    List<CommentModel> findByNews(Long newsId);

    CommentModel create(CommentModel commentModel);

    CommentModel update(Long id, CommentModel commentModel);

    void delete(Long id);

    List<CommentModel> findReplies(Long parentCommentId);
}