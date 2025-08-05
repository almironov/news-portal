package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.NewsEntityMapper;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.NewsModel;
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
    private final NewsEntityMapper newsEntityMapper;
    private final UserEntityMapper userEntityMapper;

    NewsServiceImpl(NewsRepository newsRepository, UserRepository userRepository,
                    NewsEntityMapper newsEntityMapper, UserEntityMapper userEntityMapper) {
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.newsEntityMapper = newsEntityMapper;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public NewsModel findById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));
        return newsEntityMapper.toModel(news);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsModel> findAll() {
        List<News> newsEntities = newsRepository.findAll();
        return newsEntityMapper.toModelList(newsEntities);
    }

    @Override
    public NewsModel create(NewsModel newsModel) {
        // Find the author entity
        User author = userRepository.findById(newsModel.getAuthor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", newsModel.getAuthor().getId()));

        // Convert domain model to entity
        News news = newsEntityMapper.toEntity(newsModel);
        news.setAuthor(author);
        news.setId(null); // Ensure it's a new entity

        // Save entity
        News savedNews = newsRepository.save(news);

        // Convert back to domain model and return
        return newsEntityMapper.toModel(savedNews);
    }

    @Override
    public NewsModel update(Long id, NewsModel newsModel) {
        // Find existing news entity
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("News", "id", id));

        // Update fields from domain model
        existingNews.setTitle(newsModel.getTitle());
        existingNews.setText(newsModel.getText());
        existingNews.setImageUrl(newsModel.getImageUrl());

        // Update author if changed
        if (newsModel.getAuthor() != null &&
                !existingNews.getAuthor().getId().equals(newsModel.getAuthor().getId())) {
            User newAuthor = userRepository.findById(newsModel.getAuthor().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", newsModel.getAuthor().getId()));
            existingNews.setAuthor(newAuthor);
        }

        // Save updated entity
        News updatedNews = newsRepository.save(existingNews);

        // Convert back to domain model and return
        return newsEntityMapper.toModel(updatedNews);
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
    public List<NewsModel> findByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        List<News> newsEntities = newsRepository.findByAuthor(author);
        return newsEntityMapper.toModelList(newsEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsModel> findByTitle(String titlePart) {
        List<News> newsEntities = newsRepository.findByTitleContainingIgnoreCase(titlePart);
        return newsEntityMapper.toModelList(newsEntities);
    }
}