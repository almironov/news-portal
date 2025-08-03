package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;

import java.util.List;

public interface CommentService {
    
    CommentResponseDto findById(Long id);
    
    List<CommentListItemDto> findByNews(Long newsId);
    
    CommentResponseDto create(CommentRequestDto dto);
    
    CommentResponseDto update(Long id, CommentRequestDto dto);
    
    void delete(Long id);
    
    List<CommentListItemDto> findReplies(Long parentCommentId);
}