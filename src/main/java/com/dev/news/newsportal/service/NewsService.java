package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.NewsRequestDto;
import com.dev.news.newsportal.dto.response.NewsListItemDto;
import com.dev.news.newsportal.dto.response.NewsResponseDto;

import java.util.List;

public interface NewsService {
    
    NewsResponseDto findById(Long id);
    
    List<NewsListItemDto> findAll();
    
    NewsResponseDto create(NewsRequestDto dto);
    
    NewsResponseDto update(Long id, NewsRequestDto dto);
    
    void delete(Long id);
    
    List<NewsListItemDto> findByAuthor(Long authorId);
    
    List<NewsListItemDto> findByTitle(String titlePart);
}