package com.dev.news.newsportal.mapper;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;
import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    void toEntity_shouldMapDtoToEntity() {
        // Given
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(2L)
                .build();

        // When
        Comment comment = commentMapper.toEntity(dto);

        // Then
        assertThat(comment).isNotNull();
        assertThat(comment.getId()).isNull(); // ID should be ignored in mapping
        assertThat(comment.getText()).isEqualTo(dto.getText());
        assertThat(comment.getAuthorNickname()).isEqualTo(dto.getAuthorNickname());
        assertThat(comment.getCreationDate()).isNull(); // CreationDate should be ignored in mapping
        assertThat(comment.getReplies()).isNotNull(); // Replies should be initialized as empty list
        assertThat(comment.getReplies()).isEmpty();
        
        // News and parentComment should be set by @AfterMapping method
        assertThat(comment.getNews()).isNotNull();
        assertThat(comment.getNews().getId()).isEqualTo(dto.getNewsId());
        assertThat(comment.getParentComment()).isNotNull();
        assertThat(comment.getParentComment().getId()).isEqualTo(dto.getParentCommentId());
    }

    @Test
    void toEntity_withNullParentCommentId_shouldNotSetParentComment() {
        // Given
        CommentRequestDto dto = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(null) // Explicitly set parentCommentId to null
                .build();

        // When
        Comment comment = commentMapper.toEntity(dto);

        // Then
        assertThat(comment).isNotNull();
        assertThat(comment.getNews()).isNotNull();
        assertThat(comment.getNews().getId()).isEqualTo(dto.getNewsId());
        assertThat(comment.getParentComment()).isNull(); // ParentComment should be null
    }

    @Test
    void toResponseDto_shouldMapEntityToDto() {
        // Given
        Comment reply1 = Comment.builder()
                .id(3L)
                .text("Reply 1")
                .authorNickname("user1")
                .creationDate(LocalDateTime.now().minusHours(1))
                .replies(new ArrayList<>())
                .build();

        Comment reply2 = Comment.builder()
                .id(4L)
                .text("Reply 2")
                .authorNickname("user2")
                .creationDate(LocalDateTime.now())
                .replies(new ArrayList<>())
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(LocalDateTime.now().minusDays(1))
                .replies(Arrays.asList(reply1, reply2))
                .build();

        // When
        CommentResponseDto dto = commentMapper.toResponseDto(comment);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getText()).isEqualTo(comment.getText());
        assertThat(dto.getAuthorNickname()).isEqualTo(comment.getAuthorNickname());
        assertThat(dto.getCreationDate()).isEqualTo(comment.getCreationDate());
        
        // Check replies mapping
        assertThat(dto.getReplies()).isNotNull();
        assertThat(dto.getReplies()).hasSize(2);
        assertThat(dto.getReplies().get(0).getId()).isEqualTo(reply1.getId());
        assertThat(dto.getReplies().get(0).getText()).isEqualTo(reply1.getText());
        assertThat(dto.getReplies().get(1).getId()).isEqualTo(reply2.getId());
        assertThat(dto.getReplies().get(1).getText()).isEqualTo(reply2.getText());
    }

    @Test
    void toListItemDto_shouldMapEntityToListItemDto_withReplies() {
        // Given
        Comment reply = Comment.builder()
                .id(2L)
                .text("Reply")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(LocalDateTime.now())
                .replies(Arrays.asList(reply))
                .build();

        // When
        CommentListItemDto dto = commentMapper.toListItemDto(comment);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getText()).isEqualTo(comment.getText());
        assertThat(dto.getAuthorNickname()).isEqualTo(comment.getAuthorNickname());
        assertThat(dto.getCreationDate()).isEqualTo(comment.getCreationDate());
        assertThat(dto.isHasReplies()).isTrue(); // Should be true because comment has replies
    }

    @Test
    void toListItemDto_shouldMapEntityToListItemDto_withoutReplies() {
        // Given
        Comment comment = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(LocalDateTime.now())
                .replies(new ArrayList<>()) // Empty replies list
                .build();

        // When
        CommentListItemDto dto = commentMapper.toListItemDto(comment);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getText()).isEqualTo(comment.getText());
        assertThat(dto.getAuthorNickname()).isEqualTo(comment.getAuthorNickname());
        assertThat(dto.getCreationDate()).isEqualTo(comment.getCreationDate());
        assertThat(dto.isHasReplies()).isFalse(); // Should be false because comment has no replies
    }

    @Test
    void toListItemDto_withNullReplies_shouldHandleGracefully() {
        // Given
        Comment comment = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(LocalDateTime.now())
                .replies(null) // Explicitly set replies to null
                .build();

        // When
        CommentListItemDto dto = commentMapper.toListItemDto(comment);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.isHasReplies()).isFalse(); // Should be false when replies is null
    }

    @Test
    void toListItemDtoList_shouldMapEntityListToDtoList() {
        // Given
        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Comment 1")
                .authorNickname("user1")
                .creationDate(LocalDateTime.now().minusDays(1))
                .replies(new ArrayList<>())
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .text("Comment 2")
                .authorNickname("user2")
                .creationDate(LocalDateTime.now())
                .replies(Arrays.asList(Comment.builder().id(3L).build()))
                .build();

        List<Comment> commentList = Arrays.asList(comment1, comment2);

        // When
        List<CommentListItemDto> dtoList = commentMapper.toListItemDtoList(commentList);

        // Then
        assertThat(dtoList).isNotNull();
        assertThat(dtoList).hasSize(2);
        
        // Check first comment
        assertThat(dtoList.get(0).getId()).isEqualTo(comment1.getId());
        assertThat(dtoList.get(0).getText()).isEqualTo(comment1.getText());
        assertThat(dtoList.get(0).getAuthorNickname()).isEqualTo(comment1.getAuthorNickname());
        assertThat(dtoList.get(0).isHasReplies()).isFalse();
        
        // Check second comment
        assertThat(dtoList.get(1).getId()).isEqualTo(comment2.getId());
        assertThat(dtoList.get(1).getText()).isEqualTo(comment2.getText());
        assertThat(dtoList.get(1).getAuthorNickname()).isEqualTo(comment2.getAuthorNickname());
        assertThat(dtoList.get(1).isHasReplies()).isTrue();
    }

    @Test
    void updateEntityFromDto_shouldUpdateEntityWithDtoValues() {
        // Given
        News news = News.builder()
                .id(1L)
                .title("Test News")
                .build();

        Comment parentComment = Comment.builder()
                .id(2L)
                .text("Parent comment")
                .build();

        Comment existingComment = Comment.builder()
                .id(3L)
                .text("Old text")
                .authorNickname("olduser")
                .creationDate(LocalDateTime.now().minusDays(1))
                .news(news)
                .parentComment(parentComment)
                .replies(new ArrayList<>())
                .build();

        CommentRequestDto dto = CommentRequestDto.builder()
                .text("New text")
                .authorNickname("newuser")
                .newsId(4L) // Different news ID, but news should not be updated
                .parentCommentId(5L) // Different parent comment ID, but parent comment should not be updated
                .build();

        // When
        commentMapper.updateEntityFromDto(dto, existingComment);

        // Then
        assertThat(existingComment.getId()).isEqualTo(3L); // ID should not be changed
        assertThat(existingComment.getText()).isEqualTo(dto.getText());
        assertThat(existingComment.getAuthorNickname()).isEqualTo(dto.getAuthorNickname());
        assertThat(existingComment.getCreationDate()).isNotNull(); // CreationDate should not be changed
        assertThat(existingComment.getNews()).isEqualTo(news); // News should not be changed
        assertThat(existingComment.getParentComment()).isEqualTo(parentComment); // ParentComment should not be changed
        assertThat(existingComment.getReplies()).isNotNull(); // Replies should not be changed
        assertThat(existingComment.getReplies()).isEmpty();
    }
}