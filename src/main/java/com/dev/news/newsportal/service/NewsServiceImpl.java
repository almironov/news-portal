package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.NewsCreatedApplicationEvent;
import com.dev.news.newsportal.event.NewsUpdatedApplicationEvent;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.NewsEntityMapper;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.repository.NewsRepository;
import com.dev.news.newsportal.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final NewsEntityMapper newsEntityMapper;
    private final UserEntityMapper userEntityMapper;
    private final ApplicationEventPublisher eventPublisher;

    NewsServiceImpl(NewsRepository newsRepository, UserRepository userRepository,
                    NewsEntityMapper newsEntityMapper, UserEntityMapper userEntityMapper,
                    ApplicationEventPublisher eventPublisher) {
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
        this.newsEntityMapper = newsEntityMapper;
        this.userEntityMapper = userEntityMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public NewsModel findById(Long id) {
        log.debug("Finding news by id: {}", id);
        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("News not found with id: {}", id);
                    return new ResourceNotFoundException("News", "id", id);
                });
        log.info("Successfully retrieved news with id: {}", id);
        return newsEntityMapper.toModel(news);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsModel> findAll() {
        log.debug("Finding all news");
        List<News> newsEntities = newsRepository.findAll();
        log.info("Successfully retrieved {} news items", newsEntities.size());
        return newsEntityMapper.toModelList(newsEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsModel> findAll(Pageable pageable) {
        log.debug("Finding all news with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<News> newsEntities = newsRepository.findAll(pageable);
        log.info("Successfully retrieved {} news items from page {} of {}", 
                newsEntities.getNumberOfElements(), newsEntities.getNumber(), newsEntities.getTotalPages());
        return newsEntities.map(newsEntityMapper::toModel);
    }

    @Override
    public NewsModel create(NewsModel newsModel) {
        log.debug("Creating new news with title: {}", newsModel.getTitle());
        
        // Find the author entity
        User author = userRepository.findById(newsModel.getAuthor().getId())
                .orElseThrow(() -> {
                    log.error("Author not found with id: {} when creating news", newsModel.getAuthor().getId());
                    return new ResourceNotFoundException("User", "id", newsModel.getAuthor().getId());
                });

        // Convert domain model to entity
        News news = newsEntityMapper.toEntity(newsModel);
        news.setAuthor(author);
        news.setId(null); // Ensure it's a new entity

        // Save entity
        News savedNews = newsRepository.save(news);
        log.info("Successfully created news with id: {} and title: {}", savedNews.getId(), savedNews.getTitle());

        // Publish application event
        eventPublisher.publishEvent(new NewsCreatedApplicationEvent(this, savedNews));

        // Convert back to domain model and return
        return newsEntityMapper.toModel(savedNews);
    }

    @Override
    public NewsModel update(Long id, NewsModel newsModel) {
        log.debug("Updating news with id: {} and title: {}", id, newsModel.getTitle());
        
        // Find existing news entity
        News existingNews = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("News not found with id: {} for update", id);
                    return new ResourceNotFoundException("News", "id", id);
                });

        // Use NewsEntityMapper to update properties, preserving id, creationDate, comments
        newsEntityMapper.updateEntity(existingNews, newsModel);

        // Update author if changed
        if (newsModel.getAuthor() != null &&
                !existingNews.getAuthor().getId().equals(newsModel.getAuthor().getId())) {
            log.debug("Updating author for news id: {} from {} to {}", id, 
                    existingNews.getAuthor().getId(), newsModel.getAuthor().getId());
            User newAuthor = userRepository.findById(newsModel.getAuthor().getId())
                    .orElseThrow(() -> {
                        log.error("New author not found with id: {} when updating news", newsModel.getAuthor().getId());
                        return new ResourceNotFoundException("User", "id", newsModel.getAuthor().getId());
                    });
            existingNews.setAuthor(newAuthor);
        }

        // Save updated entity
        News updatedNews = newsRepository.save(existingNews);
        log.info("Successfully updated news with id: {} and title: {}", updatedNews.getId(), updatedNews.getTitle());

        // Publish application event
        eventPublisher.publishEvent(new NewsUpdatedApplicationEvent(this, updatedNews));

        // Convert back to domain model and return
        return newsEntityMapper.toModel(updatedNews);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting news with id: {}", id);
        if (!newsRepository.existsById(id)) {
            log.warn("News not found with id: {} for deletion", id);
            throw new ResourceNotFoundException("News", "id", id);
        }
        newsRepository.deleteById(id);
        log.info("Successfully deleted news with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsModel> findByAuthor(Long authorId) {
        log.debug("Finding news by author id: {}", authorId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> {
                    log.warn("Author not found with id: {} when searching for news", authorId);
                    return new ResourceNotFoundException("User", "id", authorId);
                });

        List<News> newsEntities = newsRepository.findByAuthor(author);
        log.info("Successfully retrieved {} news items for author id: {}", newsEntities.size(), authorId);
        return newsEntityMapper.toModelList(newsEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsModel> findByTitle(String titlePart) {
        log.debug("Finding news by title containing: {}", titlePart);
        List<News> newsEntities = newsRepository.findByTitleContainingIgnoreCase(titlePart);
        log.info("Successfully retrieved {} news items matching title: {}", newsEntities.size(), titlePart);
        return newsEntityMapper.toModelList(newsEntities);
    }
}