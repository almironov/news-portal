package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.NewsCreatedApplicationEvent;
import com.dev.news.newsportal.event.NewsUpdatedApplicationEvent;
import com.dev.news.newsportal.mapper.entity.NewsEntityMapper;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.repository.NewsRepository;
import com.dev.news.newsportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for NewsServiceImpl with event publishing.
 * Tests the complete flow from service method calls to application event publishing.
 */
@ExtendWith(MockitoExtension.class)
@RecordApplicationEvents
class NewsServiceImplIntegrationTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsEntityMapper newsEntityMapper;

    @Mock
    private UserEntityMapper userEntityMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private NewsServiceImpl newsService;

    private User authorEntity;
    private News newsEntity;
    private UserModel authorModel;
    private NewsModel newsModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        newsService = new NewsServiceImpl(newsRepository, userRepository, 
                newsEntityMapper, userEntityMapper, eventPublisher);
        
        creationDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
        // Set up entity data
        authorEntity = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        newsEntity = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        // Set up domain model data
        authorModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        newsModel = NewsModel.builder()
                .id(null) // New entity
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();
    }

    @Test
    void create_shouldPublishNewsCreatedApplicationEvent() {
        // Given
        News savedNews = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        NewsModel returnedModel = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(newsEntityMapper.toEntity(newsModel)).thenReturn(newsEntity);
        when(newsRepository.save(any(News.class))).thenReturn(savedNews);
        when(newsEntityMapper.toModel(savedNews)).thenReturn(returnedModel);

        // When
        NewsModel result = newsService.create(newsModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        // Verify that application event was published
        ArgumentCaptor<NewsCreatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(NewsCreatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        NewsCreatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.getNews()).isEqualTo(savedNews);
        assertThat(publishedEvent.getSource()).isEqualTo(newsService);

        // Verify repository interactions
        verify(userRepository).findById(1L);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).toEntity(newsModel);
        verify(newsEntityMapper).toModel(savedNews);
    }

    @Test
    void update_shouldPublishNewsUpdatedApplicationEvent() {
        // Given
        Long newsId = 1L;
        LocalDateTime updateDate = LocalDateTime.now();
        
        News existingNews = News.builder()
                .id(newsId)
                .title("Original Title")
                .text("Original content")
                .imageUrl("https://example.com/original.jpg")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        News updatedNews = News.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .imageUrl("https://example.com/updated.jpg")
                .creationDate(creationDate)
                .updateDate(updateDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        NewsModel updateModel = NewsModel.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .imageUrl("https://example.com/updated.jpg")
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        NewsModel returnedModel = NewsModel.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .imageUrl("https://example.com/updated.jpg")
                .creationDate(creationDate)
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(any(News.class))).thenReturn(updatedNews);
        when(newsEntityMapper.toModel(updatedNews)).thenReturn(returnedModel);

        // When
        NewsModel result = newsService.update(newsId, updateModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(newsId);
        assertThat(result.getTitle()).isEqualTo("Updated Title");

        // Verify that application event was published
        ArgumentCaptor<NewsUpdatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(NewsUpdatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        NewsUpdatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.getNews()).isEqualTo(updatedNews);
        assertThat(publishedEvent.getSource()).isEqualTo(newsService);

        // Verify repository interactions
        verify(newsRepository).findById(newsId);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).updateEntity(existingNews, updateModel);
        verify(newsEntityMapper).toModel(updatedNews);
    }

    @Test
    void create_withAuthorChange_shouldPublishEventWithCorrectAuthor() {
        // Given
        User newAuthor = User.builder()
                .id(2L)
                .nickname("newuser")
                .email("newuser@example.com")
                .role("USER")
                .build();

        UserModel newAuthorModel = UserModel.builder()
                .id(2L)
                .nickname("newuser")
                .email("newuser@example.com")
                .role("USER")
                .build();

        NewsModel newsWithNewAuthor = NewsModel.builder()
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .author(newAuthorModel)
                .comments(new ArrayList<>())
                .build();

        News savedNewsWithNewAuthor = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(newAuthor)
                .comments(new ArrayList<>())
                .build();

        NewsModel returnedModel = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(newAuthorModel)
                .comments(new ArrayList<>())
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(newAuthor));
        when(newsEntityMapper.toEntity(newsWithNewAuthor)).thenReturn(newsEntity);
        when(newsRepository.save(any(News.class))).thenReturn(savedNewsWithNewAuthor);
        when(newsEntityMapper.toModel(savedNewsWithNewAuthor)).thenReturn(returnedModel);

        // When
        NewsModel result = newsService.create(newsWithNewAuthor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthor().getId()).isEqualTo(2L);
        assertThat(result.getAuthor().getNickname()).isEqualTo("newuser");

        // Verify that application event was published with correct author
        ArgumentCaptor<NewsCreatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(NewsCreatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        NewsCreatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getNews().getAuthor().getId()).isEqualTo(2L);
        assertThat(publishedEvent.getNews().getAuthor().getNickname()).isEqualTo("newuser");
    }

    @Test
    void update_withAuthorChange_shouldPublishEventWithNewAuthor() {
        // Given
        Long newsId = 1L;
        User newAuthor = User.builder()
                .id(3L)
                .nickname("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        UserModel newAuthorModel = UserModel.builder()
                .id(3L)
                .nickname("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        News existingNews = News.builder()
                .id(newsId)
                .title("Original Title")
                .text("Original content")
                .author(authorEntity) // Original author
                .creationDate(creationDate)
                .comments(new ArrayList<>())
                .build();

        NewsModel updateModel = NewsModel.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .author(newAuthorModel) // New author
                .comments(new ArrayList<>())
                .build();

        News updatedNews = News.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .creationDate(creationDate)
                .updateDate(LocalDateTime.now())
                .author(newAuthor) // New author
                .comments(new ArrayList<>())
                .build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newAuthor));
        when(newsRepository.save(any(News.class))).thenReturn(updatedNews);
        when(newsEntityMapper.toModel(updatedNews)).thenReturn(updateModel);

        // When
        NewsModel result = newsService.update(newsId, updateModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthor().getId()).isEqualTo(3L);

        // Verify that application event was published with new author
        ArgumentCaptor<NewsUpdatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(NewsUpdatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        NewsUpdatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getNews().getAuthor().getId()).isEqualTo(3L);
        assertThat(publishedEvent.getNews().getAuthor().getNickname()).isEqualTo("updateduser");

        // Verify author was updated
        verify(userRepository).findById(3L);
    }

    @Test
    void create_multipleNews_shouldPublishMultipleEvents() {
        // Given
        News savedNews1 = News.builder().id(1L).title("News 1").text("Content 1")
                .creationDate(creationDate).author(authorEntity).comments(new ArrayList<>()).build();
        News savedNews2 = News.builder().id(2L).title("News 2").text("Content 2")
                .creationDate(creationDate).author(authorEntity).comments(new ArrayList<>()).build();

        NewsModel returnedModel1 = NewsModel.builder().id(1L).title("News 1").text("Content 1")
                .creationDate(creationDate).author(authorModel).comments(new ArrayList<>()).build();
        NewsModel returnedModel2 = NewsModel.builder().id(2L).title("News 2").text("Content 2")
                .creationDate(creationDate).author(authorModel).comments(new ArrayList<>()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(newsEntityMapper.toEntity(any(NewsModel.class))).thenReturn(newsEntity);
        when(newsRepository.save(any(News.class))).thenReturn(savedNews1, savedNews2);
        when(newsEntityMapper.toModel(savedNews1)).thenReturn(returnedModel1);
        when(newsEntityMapper.toModel(savedNews2)).thenReturn(returnedModel2);

        // When
        newsService.create(newsModel);
        newsService.create(newsModel);

        // Then
        verify(eventPublisher, times(2)).publishEvent(any(NewsCreatedApplicationEvent.class));
        verify(newsRepository, times(2)).save(any(News.class));
    }

    @Test
    void delete_shouldNotPublishAnyEvent() {
        // Given
        Long newsId = 1L;
        when(newsRepository.existsById(newsId)).thenReturn(true);

        // When
        newsService.delete(newsId);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
        verify(newsRepository).existsById(newsId);
        verify(newsRepository).deleteById(newsId);
    }

    @Test
    void findById_shouldNotPublishAnyEvent() {
        // Given
        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(newsEntityMapper.toModel(newsEntity)).thenReturn(newsModel);

        // When
        newsService.findById(1L);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
        verify(newsRepository).findById(1L);
    }
}