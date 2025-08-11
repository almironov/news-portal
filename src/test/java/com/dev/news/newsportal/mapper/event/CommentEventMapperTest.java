package com.dev.news.newsportal.mapper.event;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.CommentCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CommentEventMapper.
 * Tests the mapping from Comment entities to event records.
 */
class CommentEventMapperTest {

    private CommentEventMapper commentEventMapper;
    private User author;
    private News news;
    private Comment parentComment;
    private Comment comment;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        commentEventMapper = Mappers.getMapper(CommentEventMapper.class);
        
        creationDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
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
                .author(author)
                .comments(new ArrayList<>())
                .build();

        parentComment = Comment.builder()
                .id(50L)
                .text("This is a parent comment")
                .creationDate(creationDate.minusHours(1))
                .authorNickname("parentuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
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
    void toCommentCreatedEvent_shouldMapAllFields() {
        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.commentId()).isEqualTo(200L);
        assertThat(event.text()).isEqualTo("This is a test comment");
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorNickname()).isEqualTo("commentuser");
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.newsTitle()).isEqualTo("Test News Title");
        assertThat(event.parentCommentId()).isNull();
        assertThat(event.eventTimestamp()).isNotNull();
        assertThat(event.eventTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void toCommentCreatedEvent_withParentComment_shouldMapCorrectly() {
        // Given
        comment.setParentComment(parentComment);

        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.commentId()).isEqualTo(200L);
        assertThat(event.text()).isEqualTo("This is a test comment");
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorNickname()).isEqualTo("commentuser");
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.newsTitle()).isEqualTo("Test News Title");
        assertThat(event.parentCommentId()).isEqualTo(50L);
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void toCommentCreatedEvent_withLongText_shouldMapCorrectly() {
        // Given
        String longText = "This is a very long comment text that contains multiple sentences. " +
                "It should be properly mapped to the event record without any truncation. " +
                "The mapper should handle long text content correctly and preserve all characters.";
        comment.setText(longText);

        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.commentId()).isEqualTo(200L);
        assertThat(event.text()).isEqualTo(longText);
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorNickname()).isEqualTo("commentuser");
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.newsTitle()).isEqualTo("Test News Title");
        assertThat(event.parentCommentId()).isNull();
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void toCommentCreatedEvent_withSpecialCharacters_shouldMapCorrectly() {
        // Given
        String textWithSpecialChars = "Comment with special chars: @#$%^&*()_+{}|:<>?[]\\;'\",./ and émojis 😀🎉";
        String nicknameWithSpecialChars = "user@domain.com";
        comment.setText(textWithSpecialChars);
        comment.setAuthorNickname(nicknameWithSpecialChars);

        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.commentId()).isEqualTo(200L);
        assertThat(event.text()).isEqualTo(textWithSpecialChars);
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorNickname()).isEqualTo(nicknameWithSpecialChars);
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.newsTitle()).isEqualTo("Test News Title");
        assertThat(event.parentCommentId()).isNull();
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void eventTimestamp_shouldBeCurrentTime() {
        // Given
        LocalDateTime beforeMapping = LocalDateTime.now().minusSeconds(1);
        
        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);
        
        // Then
        LocalDateTime afterMapping = LocalDateTime.now().plusSeconds(1);
        
        assertThat(event.eventTimestamp()).isAfter(beforeMapping);
        assertThat(event.eventTimestamp()).isBefore(afterMapping);
    }

    @Test
    void toString_shouldReturnFormattedString() {
        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        String eventString = event.toString();
        assertThat(eventString).contains("CommentCreatedEvent");
        assertThat(eventString).contains("commentId=200");
        assertThat(eventString).contains("newsId=100");
        assertThat(eventString).contains("newsTitle='Test News Title'");
        assertThat(eventString).contains("authorNickname='commentuser'");
        assertThat(eventString).contains("parentCommentId=null");
    }

    @Test
    void toString_withParentComment_shouldIncludeParentId() {
        // Given
        comment.setParentComment(parentComment);

        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        String eventString = event.toString();
        assertThat(eventString).contains("CommentCreatedEvent");
        assertThat(eventString).contains("commentId=200");
        assertThat(eventString).contains("newsId=100");
        assertThat(eventString).contains("newsTitle='Test News Title'");
        assertThat(eventString).contains("authorNickname='commentuser'");
        assertThat(eventString).contains("parentCommentId=50");
    }

    @Test
    void toCommentCreatedEvent_withDifferentNewsTitle_shouldMapCorrectly() {
        // Given
        news.setTitle("Different News Title with Special Characters & Numbers 123");

        // When
        CommentCreatedEvent event = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        assertThat(event).isNotNull();
        assertThat(event.commentId()).isEqualTo(200L);
        assertThat(event.text()).isEqualTo("This is a test comment");
        assertThat(event.creationDate()).isEqualTo(creationDate);
        assertThat(event.authorNickname()).isEqualTo("commentuser");
        assertThat(event.newsId()).isEqualTo(100L);
        assertThat(event.newsTitle()).isEqualTo("Different News Title with Special Characters & Numbers 123");
        assertThat(event.parentCommentId()).isNull();
        assertThat(event.eventTimestamp()).isNotNull();
    }

    @Test
    void toCommentCreatedEvent_multipleCallsShouldHaveDifferentTimestamps() throws InterruptedException {
        // When
        CommentCreatedEvent event1 = commentEventMapper.toCommentCreatedEvent(comment);
        Thread.sleep(10); // Small delay to ensure different timestamps
        CommentCreatedEvent event2 = commentEventMapper.toCommentCreatedEvent(comment);

        // Then
        assertThat(event1.eventTimestamp()).isNotEqualTo(event2.eventTimestamp());
        assertThat(event1.eventTimestamp()).isBefore(event2.eventTimestamp());
        
        // All other fields should be the same
        assertThat(event1.commentId()).isEqualTo(event2.commentId());
        assertThat(event1.text()).isEqualTo(event2.text());
        assertThat(event1.creationDate()).isEqualTo(event2.creationDate());
        assertThat(event1.authorNickname()).isEqualTo(event2.authorNickname());
        assertThat(event1.newsId()).isEqualTo(event2.newsId());
        assertThat(event1.newsTitle()).isEqualTo(event2.newsTitle());
        assertThat(event1.parentCommentId()).isEqualTo(event2.parentCommentId());
    }
}