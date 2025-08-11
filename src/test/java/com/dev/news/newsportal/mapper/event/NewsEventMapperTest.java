package com.dev.news.newsportal.mapper.event;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.NewsCreatedEvent;
import com.dev.news.newsportal.event.NewsUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NewsEventMapper.
 * Tests the mapping from News entities to event records.
 */
class NewsEventMapperTest {

    private NewsEventMapper newsEventMapper;
    private User author;
    private News news;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @BeforeEach
    void setUp() {
        newsEventMapper = Mappers.getMapper(NewsEventMapper.class);
        
        creationDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        updateDate = LocalDateTime.of(2024, 1, 16, 14, 45, 0);
        
        author = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        news = News.builder()
                .id(100L)
                .title("Test News Title")
                .text("This is a test news article content")
                .imageUrl("https://example.com/test-image.jpg")
                .creationDate(creationDate)
                .updateDate(updateDate)
                .author(author)
                .comments(new ArrayList<>())
                .build();
    }

    @Test
    void toNewsCreatedEvent_shouldMapAllFields() {
        // When
        NewsCreatedEvent event = newsEventMapper.toNewsCreatedEvent(news);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.title()).isEqualTo("Test News Title");
        assertThat(event.text()).isEqualTo("This is a test news article content");
        assertThat(event.imageUrl()).isEqualTo("https://example.com/test-image.jpg");
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorId()).isEqualTo(1L);
        assertThat(event.authorNickname()).isEqualTo("testuser");
        assertThat(event.eventTimestamp()).isNotNull();
        assertThat(event.eventTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void toNewsCreatedEvent_withNullImageUrl_shouldMapCorrectly() {
        // Given
        news.setImageUrl(null);

        // When
        NewsCreatedEvent event = newsEventMapper.toNewsCreatedEvent(news);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.title()).isEqualTo("Test News Title");
        assertThat(event.text()).isEqualTo("This is a test news article content");
        assertThat(event.imageUrl()).isNull();
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorId()).isEqualTo(1L);
        assertThat(event.authorNickname()).isEqualTo("testuser");
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void toNewsUpdatedEvent_shouldMapAllFields() {
        // When
        NewsUpdatedEvent event = newsEventMapper.toNewsUpdatedEvent(news);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.title()).isEqualTo("Test News Title");
        assertThat(event.text()).isEqualTo("This is a test news article content");
        assertThat(event.imageUrl()).isEqualTo("https://example.com/test-image.jpg");
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.updateDate()).isEqualTo(updateDate);
        assertThat(event.authorId()).isEqualTo(1L);
        assertThat(event.authorNickname()).isEqualTo("testuser");
        assertThat(event.eventTimestamp()).isNotNull();
        assertThat(event.eventTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void toNewsUpdatedEvent_withNullUpdateDate_shouldMapCorrectly() {
        // Given
        news.setUpdateDate(null);

        // When
        NewsUpdatedEvent event = newsEventMapper.toNewsUpdatedEvent(news);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.title()).isEqualTo("Test News Title");
        assertThat(event.text()).isEqualTo("This is a test news article content");
        assertThat(event.imageUrl()).isEqualTo("https://example.com/test-image.jpg");
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.updateDate()).isNull();
        assertThat(event.authorId()).isEqualTo(1L);
        assertThat(event.authorNickname()).isEqualTo("testuser");
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void toNewsUpdatedEvent_withNullImageUrl_shouldMapCorrectly() {
        // Given
        news.setImageUrl(null);

        // When
        NewsUpdatedEvent event = newsEventMapper.toNewsUpdatedEvent(news);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.title()).isEqualTo("Test News Title");
        assertThat(event.text()).isEqualTo("This is a test news article content");
        assertThat(event.imageUrl()).isNull();
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.updateDate()).isEqualTo(updateDate);
        assertThat(event.authorId()).isEqualTo(1L);
        assertThat(event.authorNickname()).isEqualTo("testuser");
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void eventTimestamp_shouldBeCurrentTime() {
        // Given
        LocalDateTime beforeMapping = LocalDateTime.now().minusSeconds(1);
        
        // When
        NewsCreatedEvent createdEvent = newsEventMapper.toNewsCreatedEvent(news);
        NewsUpdatedEvent updatedEvent = newsEventMapper.toNewsUpdatedEvent(news);
        
        // Then
        LocalDateTime afterMapping = LocalDateTime.now().plusSeconds(1);
        
        assertThat(createdEvent.eventTimestamp()).isAfter(beforeMapping);
        assertThat(createdEvent.eventTimestamp()).isBefore(afterMapping);
        
        assertThat(updatedEvent.eventTimestamp()).isAfter(beforeMapping);
        assertThat(updatedEvent.eventTimestamp()).isBefore(afterMapping);
    }

    @Test
    void toString_shouldReturnFormattedString() {
        // When
        NewsCreatedEvent createdEvent = newsEventMapper.toNewsCreatedEvent(news);
        NewsUpdatedEvent updatedEvent = newsEventMapper.toNewsUpdatedEvent(news);

        // Then
        String createdString = createdEvent.toString();
        assertThat(createdString).contains("NewsCreatedEvent");
        assertThat(createdString).contains("newsId=100");
        assertThat(createdString).contains("title='Test News Title'");
        assertThat(createdString).contains("authorId=1");
        assertThat(createdString).contains("authorNickname='testuser'");

        String updatedString = updatedEvent.toString();
        assertThat(updatedString).contains("NewsUpdatedEvent");
        assertThat(updatedString).contains("newsId=100");
        assertThat(updatedString).contains("title='Test News Title'");
        assertThat(updatedString).contains("authorId=1");
        assertThat(updatedString).contains("authorNickname='testuser'");
        assertThat(updatedString).contains("updateDate=" + updateDate);
    }
}