package com.dev.news.newsportal.service.rabbitmq;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.*;
import com.dev.news.newsportal.mapper.event.CommentEventMapper;
import com.dev.news.newsportal.mapper.event.NewsEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for EventListenerService to verify @TransactionalEventListener behavior.
 * Tests successful event processing, error handling, and interaction with EventPublisher.
 */
@ExtendWith(MockitoExtension.class)
class EventListenerServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private NewsEventMapper newsEventMapper;

    @Mock
    private CommentEventMapper commentEventMapper;

    private EventListenerService eventListenerService;

    private User author;
    private News news;
    private Comment comment;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @BeforeEach
    void setUp() {
        eventListenerService = new EventListenerService(eventPublisher, newsEventMapper, commentEventMapper);

        creationDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        updateDate = LocalDateTime.of(2024, 1, 15, 14, 45, 0);

        // Set up entity data
        author = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        news = News.builder()
                .id(100L)
                .title("Test News Title")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .updateDate(updateDate)
                .author(author)
                .comments(new ArrayList<>())
                .build();

        comment = Comment.builder()
                .id(200L)
                .text("This is a test comment")
                .creationDate(creationDate)
                .authorNickname("commentuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();
    }

    @Test
    void handleNewsCreated_shouldMapAndPublishEvent() {
        // Given
        NewsCreatedApplicationEvent applicationEvent = new NewsCreatedApplicationEvent(this, news);
        NewsCreatedEvent expectedEvent = new NewsCreatedEvent(
                100L,
                "Test News Title",
                "This is a test news article",
                "https://example.com/image.jpg",
                creationDate,
                1L,
                "testuser",
                LocalDateTime.now()
        );

        when(newsEventMapper.toNewsCreatedEvent(news)).thenReturn(expectedEvent);

        // When
        eventListenerService.handleNewsCreated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsCreatedEvent(news);
        verify(eventPublisher).publishNewsCreatedEvent(expectedEvent);
    }

    @Test
    void handleNewsUpdated_shouldMapAndPublishEvent() {
        // Given
        NewsUpdatedApplicationEvent applicationEvent = new NewsUpdatedApplicationEvent(this, news);
        NewsUpdatedEvent expectedEvent = new NewsUpdatedEvent(
                100L,
                "Test News Title",
                "This is a test news article",
                "https://example.com/image.jpg",
                creationDate,
                updateDate,
                1L,
                "testuser",
                LocalDateTime.now()
        );

        when(newsEventMapper.toNewsUpdatedEvent(news)).thenReturn(expectedEvent);

        // When
        eventListenerService.handleNewsUpdated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsUpdatedEvent(news);
        verify(eventPublisher).publishNewsUpdatedEvent(expectedEvent);
    }

    @Test
    void handleCommentCreated_shouldMapAndPublishEvent() {
        // Given
        CommentCreatedApplicationEvent applicationEvent = new CommentCreatedApplicationEvent(this, comment);
        CommentCreatedEvent expectedEvent = new CommentCreatedEvent(
                200L,
                "This is a test comment",
                creationDate,
                "commentuser",
                100L,
                "Test News Title",
                null,
                LocalDateTime.now()
        );

        when(commentEventMapper.toCommentCreatedEvent(comment)).thenReturn(expectedEvent);

        // When
        eventListenerService.handleCommentCreated(applicationEvent);

        // Then
        verify(commentEventMapper).toCommentCreatedEvent(comment);
        verify(eventPublisher).publishCommentCreatedEvent(expectedEvent);
    }

    @Test
    void handleNewsCreated_whenMapperThrowsException_shouldLogErrorAndNotPublish() {
        // Given
        NewsCreatedApplicationEvent applicationEvent = new NewsCreatedApplicationEvent(this, news);
        RuntimeException mapperException = new RuntimeException("Mapper failed");

        when(newsEventMapper.toNewsCreatedEvent(news)).thenThrow(mapperException);

        // When
        eventListenerService.handleNewsCreated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsCreatedEvent(news);
        verify(eventPublisher, never()).publishNewsCreatedEvent(any());

        // The method should not re-throw the exception (it's logged but swallowed)
        // This is expected behavior for @TransactionalEventListener to avoid affecting the main transaction
    }

    @Test
    void handleNewsCreated_whenEventPublisherThrowsException_shouldLogErrorAndNotRethrow() {
        // Given
        NewsCreatedApplicationEvent applicationEvent = new NewsCreatedApplicationEvent(this, news);
        NewsCreatedEvent newsCreatedEvent = new NewsCreatedEvent(
                100L, "Test News Title", "This is a test news article", "https://example.com/image.jpg",
                creationDate, 1L, "testuser", LocalDateTime.now()
        );
        RuntimeException publisherException = new RuntimeException("Publisher failed");

        when(newsEventMapper.toNewsCreatedEvent(news)).thenReturn(newsCreatedEvent);
        doThrow(publisherException).when(eventPublisher).publishNewsCreatedEvent(newsCreatedEvent);

        // When
        eventListenerService.handleNewsCreated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsCreatedEvent(news);
        verify(eventPublisher).publishNewsCreatedEvent(newsCreatedEvent);

        // The method should not re-throw the exception (it's logged but swallowed)
        // This is expected behavior for @TransactionalEventListener to avoid affecting the main transaction
    }

    @Test
    void handleNewsUpdated_whenMapperThrowsException_shouldLogErrorAndNotPublish() {
        // Given
        NewsUpdatedApplicationEvent applicationEvent = new NewsUpdatedApplicationEvent(this, news);
        RuntimeException mapperException = new RuntimeException("Mapper failed");

        when(newsEventMapper.toNewsUpdatedEvent(news)).thenThrow(mapperException);

        // When
        eventListenerService.handleNewsUpdated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsUpdatedEvent(news);
        verify(eventPublisher, never()).publishNewsUpdatedEvent(any());
    }

    @Test
    void handleNewsUpdated_whenEventPublisherThrowsException_shouldLogErrorAndNotRethrow() {
        // Given
        NewsUpdatedApplicationEvent applicationEvent = new NewsUpdatedApplicationEvent(this, news);
        NewsUpdatedEvent newsUpdatedEvent = new NewsUpdatedEvent(
                100L, "Test News Title", "This is a test news article", "https://example.com/image.jpg",
                creationDate, updateDate, 1L, "testuser", LocalDateTime.now()
        );
        RuntimeException publisherException = new RuntimeException("Publisher failed");

        when(newsEventMapper.toNewsUpdatedEvent(news)).thenReturn(newsUpdatedEvent);
        doThrow(publisherException).when(eventPublisher).publishNewsUpdatedEvent(newsUpdatedEvent);

        // When
        eventListenerService.handleNewsUpdated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsUpdatedEvent(news);
        verify(eventPublisher).publishNewsUpdatedEvent(newsUpdatedEvent);
    }

    @Test
    void handleCommentCreated_whenMapperThrowsException_shouldLogErrorAndNotPublish() {
        // Given
        CommentCreatedApplicationEvent applicationEvent = new CommentCreatedApplicationEvent(this, comment);
        RuntimeException mapperException = new RuntimeException("Mapper failed");

        when(commentEventMapper.toCommentCreatedEvent(comment)).thenThrow(mapperException);

        // When
        eventListenerService.handleCommentCreated(applicationEvent);

        // Then
        verify(commentEventMapper).toCommentCreatedEvent(comment);
        verify(eventPublisher, never()).publishCommentCreatedEvent(any());
    }

    @Test
    void handleCommentCreated_whenEventPublisherThrowsException_shouldLogErrorAndNotRethrow() {
        // Given
        CommentCreatedApplicationEvent applicationEvent = new CommentCreatedApplicationEvent(this, comment);
        CommentCreatedEvent commentCreatedEvent = new CommentCreatedEvent(
                200L, "This is a test comment", creationDate, "commentuser",
                100L, "Test News Title", null, LocalDateTime.now()
        );
        RuntimeException publisherException = new RuntimeException("Publisher failed");

        when(commentEventMapper.toCommentCreatedEvent(comment)).thenReturn(commentCreatedEvent);
        doThrow(publisherException).when(eventPublisher).publishCommentCreatedEvent(commentCreatedEvent);

        // When
        eventListenerService.handleCommentCreated(applicationEvent);

        // Then
        verify(commentEventMapper).toCommentCreatedEvent(comment);
        verify(eventPublisher).publishCommentCreatedEvent(commentCreatedEvent);
    }

    @Test
    void handleNewsCreated_withCommentWithParent_shouldMapAndPublishCorrectly() {
        // Given
        Comment parentComment = Comment.builder()
                .id(150L)
                .text("Parent comment")
                .creationDate(creationDate.minusHours(1))
                .authorNickname("parentuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        Comment replyComment = Comment.builder()
                .id(250L)
                .text("Reply comment")
                .creationDate(creationDate)
                .authorNickname("replyuser")
                .news(news)
                .parentComment(parentComment)
                .replies(new ArrayList<>())
                .build();

        CommentCreatedApplicationEvent applicationEvent = new CommentCreatedApplicationEvent(this, replyComment);
        CommentCreatedEvent expectedEvent = new CommentCreatedEvent(
                250L,
                "Reply comment",
                creationDate,
                "replyuser",
                100L,
                "Test News Title",
                150L, // parent comment ID
                LocalDateTime.now()
        );

        when(commentEventMapper.toCommentCreatedEvent(replyComment)).thenReturn(expectedEvent);

        // When
        eventListenerService.handleCommentCreated(applicationEvent);

        // Then
        verify(commentEventMapper).toCommentCreatedEvent(replyComment);

        ArgumentCaptor<CommentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(CommentCreatedEvent.class);
        verify(eventPublisher).publishCommentCreatedEvent(eventCaptor.capture());

        CommentCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.parentCommentId()).isEqualTo(150L);
        assertThat(capturedEvent.commentId()).isEqualTo(250L);
        assertThat(capturedEvent.authorNickname()).isEqualTo("replyuser");
    }

    @Test
    void handleNewsUpdated_withNullUpdateDate_shouldMapAndPublishCorrectly() {
        // Given
        News newsWithoutUpdateDate = News.builder()
                .id(100L)
                .title("Test News Title")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .updateDate(null) // No update date
                .author(author)
                .comments(new ArrayList<>())
                .build();

        NewsUpdatedApplicationEvent applicationEvent = new NewsUpdatedApplicationEvent(this, newsWithoutUpdateDate);
        NewsUpdatedEvent expectedEvent = new NewsUpdatedEvent(
                100L, "Test News Title", "This is a test news article", "https://example.com/image.jpg",
                creationDate, null, 1L, "testuser", LocalDateTime.now()
        );

        when(newsEventMapper.toNewsUpdatedEvent(newsWithoutUpdateDate)).thenReturn(expectedEvent);

        // When
        eventListenerService.handleNewsUpdated(applicationEvent);

        // Then
        verify(newsEventMapper).toNewsUpdatedEvent(newsWithoutUpdateDate);

        ArgumentCaptor<NewsUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(NewsUpdatedEvent.class);
        verify(eventPublisher).publishNewsUpdatedEvent(eventCaptor.capture());

        NewsUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.updateDate()).isNull();
        assertThat(capturedEvent.newsId()).isEqualTo(100L);
    }

    @Test
    void allEventHandlers_shouldCallCorrectMapperMethods() {
        // Given
        NewsCreatedApplicationEvent newsCreatedEvent = new NewsCreatedApplicationEvent(this, news);
        NewsUpdatedApplicationEvent newsUpdatedEvent = new NewsUpdatedApplicationEvent(this, news);
        CommentCreatedApplicationEvent commentCreatedEvent = new CommentCreatedApplicationEvent(this, comment);

        when(newsEventMapper.toNewsCreatedEvent(any())).thenReturn(mock(NewsCreatedEvent.class));
        when(newsEventMapper.toNewsUpdatedEvent(any())).thenReturn(mock(NewsUpdatedEvent.class));
        when(commentEventMapper.toCommentCreatedEvent(any())).thenReturn(mock(CommentCreatedEvent.class));

        // When
        eventListenerService.handleNewsCreated(newsCreatedEvent);
        eventListenerService.handleNewsUpdated(newsUpdatedEvent);
        eventListenerService.handleCommentCreated(commentCreatedEvent);

        // Then
        verify(newsEventMapper, times(1)).toNewsCreatedEvent(news);
        verify(newsEventMapper, times(1)).toNewsUpdatedEvent(news);
        verify(commentEventMapper, times(1)).toCommentCreatedEvent(comment);

        verify(eventPublisher, times(1)).publishNewsCreatedEvent(any());
        verify(eventPublisher, times(1)).publishNewsUpdatedEvent(any());
        verify(eventPublisher, times(1)).publishCommentCreatedEvent(any());
    }
}