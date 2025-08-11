package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.CommentCreatedApplicationEvent;
import com.dev.news.newsportal.event.NewsCreatedApplicationEvent;
import com.dev.news.newsportal.event.NewsUpdatedApplicationEvent;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.CommentEntityMapper;
import com.dev.news.newsportal.mapper.entity.NewsEntityMapper;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.repository.CommentRepository;
import com.dev.news.newsportal.repository.NewsRepository;
import com.dev.news.newsportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for transaction rollback scenarios to verify that events are not published
 * when database operations fail and transactions are rolled back.
 * This tests the @TransactionalEventListener behavior.
 */
@ExtendWith(MockitoExtension.class)
@RecordApplicationEvents
class TransactionRollbackEventTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsEntityMapper newsEntityMapper;

    @Mock
    private UserEntityMapper userEntityMapper;

    @Mock
    private CommentEntityMapper commentEntityMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private NewsServiceImpl newsService;
    private CommentServiceImpl commentService;

    private User authorEntity;
    private News newsEntity;
    private UserModel authorModel;
    private NewsModel newsModel;
    private CommentModel commentModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        newsService = new NewsServiceImpl(newsRepository, userRepository, 
                newsEntityMapper, userEntityMapper, eventPublisher);
        commentService = new CommentServiceImpl(commentRepository, newsRepository, 
                commentEntityMapper, eventPublisher);
        
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
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        commentModel = CommentModel.builder()
                .text("Test comment")
                .authorNickname("commentuser")
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();
    }

    @Test
    void newsCreate_whenRepositorySaveThrowsException_shouldNotPublishEvent() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(newsEntityMapper.toEntity(newsModel)).thenReturn(newsEntity);
        when(newsRepository.save(any(News.class)))
                .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

        // When/Then
        assertThatThrownBy(() -> newsService.create(newsModel))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Database constraint violation");

        // Verify that no application event was published due to transaction rollback
        verify(eventPublisher, never()).publishEvent(any(NewsCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions occurred
        verify(userRepository).findById(1L);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).toEntity(newsModel);
    }

    @Test
    void newsCreate_whenUserNotFound_shouldNotPublishEvent() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.create(newsModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 1");

        // Verify that no application event was published
        verify(eventPublisher, never()).publishEvent(any(NewsCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions
        verify(userRepository).findById(1L);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void newsUpdate_whenRepositorySaveThrowsException_shouldNotPublishEvent() {
        // Given
        Long newsId = 1L;
        News existingNews = News.builder()
                .id(newsId)
                .title("Original Title")
                .text("Original content")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        NewsModel updateModel = NewsModel.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(any(News.class)))
                .thenThrow(new DataIntegrityViolationException("Update constraint violation"));

        // When/Then
        assertThatThrownBy(() -> newsService.update(newsId, updateModel))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Update constraint violation");

        // Verify that no application event was published due to transaction rollback
        verify(eventPublisher, never()).publishEvent(any(NewsUpdatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions occurred
        verify(newsRepository).findById(newsId);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).updateEntity(existingNews, updateModel);
    }

    @Test
    void newsUpdate_whenNewsNotFound_shouldNotPublishEvent() {
        // Given
        Long newsId = 999L;
        NewsModel updateModel = NewsModel.builder()
                .id(newsId)
                .title("Updated Title")
                .text("Updated content")
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        when(newsRepository.findById(newsId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.update(newsId, updateModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 999");

        // Verify that no application event was published
        verify(eventPublisher, never()).publishEvent(any(NewsUpdatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions
        verify(newsRepository).findById(newsId);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void commentCreate_whenRepositorySaveThrowsException_shouldNotPublishEvent() {
        // Given
        News news = News.builder()
                .id(1L)
                .title("Test News")
                .text("Test content")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        Comment comment = Comment.builder()
                .text("Test comment")
                .creationDate(creationDate)
                .authorNickname("commentuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentEntityMapper.toEntity(commentModel)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class)))
                .thenThrow(new DataIntegrityViolationException("Comment constraint violation"));

        // When/Then
        assertThatThrownBy(() -> commentService.create(commentModel))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Comment constraint violation");

        // Verify that no application event was published due to transaction rollback
        verify(eventPublisher, never()).publishEvent(any(CommentCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions occurred
        verify(newsRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
        verify(commentEntityMapper).toEntity(commentModel);
    }

    @Test
    void commentCreate_whenNewsNotFound_shouldNotPublishEvent() {
        // Given
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.create(commentModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 1");

        // Verify that no application event was published
        verify(eventPublisher, never()).publishEvent(any(CommentCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions
        verify(newsRepository).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void commentCreate_whenParentCommentNotFound_shouldNotPublishEvent() {
        // Given
        CommentModel replyModel = CommentModel.builder()
                .text("Reply comment")
                .authorNickname("replyuser")
                .newsId(1L)
                .parentCommentId(999L) // Non-existent parent
                .replies(new ArrayList<>())
                .build();

        News news = News.builder()
                .id(1L)
                .title("Test News")
                .text("Test content")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.create(replyModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");

        // Verify that no application event was published
        verify(eventPublisher, never()).publishEvent(any(CommentCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions
        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void commentCreate_whenParentCommentBelongsToDifferentNews_shouldNotPublishEvent() {
        // Given
        News news1 = News.builder()
                .id(1L)
                .title("Test News 1")
                .text("Test content 1")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        News news2 = News.builder()
                .id(2L)
                .title("Test News 2")
                .text("Test content 2")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        Comment parentComment = Comment.builder()
                .id(50L)
                .text("Parent comment")
                .creationDate(creationDate)
                .authorNickname("parentuser")
                .news(news2) // Different news
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        CommentModel replyModel = CommentModel.builder()
                .text("Reply comment")
                .authorNickname("replyuser")
                .newsId(1L) // News 1
                .parentCommentId(50L) // Parent belongs to News 2
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news1));
        when(commentRepository.findById(50L)).thenReturn(Optional.of(parentComment));

        // When/Then
        assertThatThrownBy(() -> commentService.create(replyModel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parent comment does not belong to the specified news");

        // Verify that no application event was published
        verify(eventPublisher, never()).publishEvent(any(CommentCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions
        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(50L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void newsCreate_whenRuntimeExceptionOccurs_shouldNotPublishEvent() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(newsEntityMapper.toEntity(newsModel)).thenReturn(newsEntity);
        when(newsRepository.save(any(News.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));

        // When/Then
        assertThatThrownBy(() -> newsService.create(newsModel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unexpected database error");

        // Verify that no application event was published due to transaction rollback
        verify(eventPublisher, never()).publishEvent(any(NewsCreatedApplicationEvent.class));
        verify(eventPublisher, never()).publishEvent(any());

        // Verify repository interactions occurred
        verify(userRepository).findById(1L);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).toEntity(newsModel);
    }
}