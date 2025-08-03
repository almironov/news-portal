package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.NewsRequestDto;
import com.dev.news.newsportal.dto.response.NewsListItemDto;
import com.dev.news.newsportal.dto.response.NewsResponseDto;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.NewsMapper;
import com.dev.news.newsportal.repository.NewsRepository;
import com.dev.news.newsportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
class NewsServiceImpl implements NewsService {
    
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsMapper newsMapper;
    
    NewsServiceImpl(NewsRepository newsRepository, UserRepository userRepository, NewsMapper newsMapper) {
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.newsMapper = newsMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public NewsResponseDto findById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));
        return newsMapper.toResponseDto(news);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NewsListItemDto> findAll() {
        return newsMapper.toListItemDtoList(newsRepository.findAll());
    }
    
    @Override
    public NewsResponseDto create(NewsRequestDto dto) {
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAuthorId()));
        
        News news = newsMapper.toEntity(dto);
        news.setAuthor(author);
        
        News savedNews = newsRepository.save(news);
        return newsMapper.toResponseDto(savedNews);
    }
    
    @Override
    public NewsResponseDto update(Long id, NewsRequestDto dto) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));
        
        newsMapper.updateEntityFromDto(dto, news);
        
        if (!news.getAuthor().getId().equals(dto.getAuthorId())) {
            User newAuthor = userRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAuthorId()));
            news.setAuthor(newAuthor);
        }
        
        News updatedNews = newsRepository.save(news);
        return newsMapper.toResponseDto(updatedNews);
    }
    
    @Override
    public void delete(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new ResourceNotFoundException("News", "id", id);
        }
        newsRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NewsListItemDto> findByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));
        
        return newsMapper.toListItemDtoList(newsRepository.findByAuthor(author));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NewsListItemDto> findByTitle(String titlePart) {
        return newsMapper.toListItemDtoList(newsRepository.findByTitleContainingIgnoreCase(titlePart));
    }
}