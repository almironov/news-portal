package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;
import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.CommentMapper;
import com.dev.news.newsportal.repository.CommentRepository;
import com.dev.news.newsportal.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private News news;
    private Comment comment;
    private Comment parentComment;
    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;
    private CommentListItemDto commentListItemDto;

    @BeforeEach
    void setUp() {
        // Set up test data
        news = News.builder()
                .id(1L)
                .title("Test News")
                .build();

        parentComment = Comment.builder()
                .id(2L)
                .text("Parent comment")
                .authorNickname("parentuser")
                .creationDate(LocalDateTime.now().minusDays(1))
                .news(news)
                .replies(new ArrayList<>())
                .build();

        comment = Comment.builder()
                .id(3L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(LocalDateTime.now())
                .news(news)
                .parentComment(parentComment)
                .replies(new ArrayList<>())
                .build();

        commentRequestDto = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(2L)
                .build();

        commentResponseDto = CommentResponseDto.builder()
                .id(3L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(comment.getCreationDate())
                .replies(new ArrayList<>())
                .build();

        commentListItemDto = CommentListItemDto.builder()
                .id(3L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(comment.getCreationDate())
                .hasReplies(false)
                .build();
    }

    @Test
    void findById_withExistingId_shouldReturnCommentResponseDto() {
        // Given
        when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));
        when(commentMapper.toResponseDto(comment)).thenReturn(commentResponseDto);

        // When
        CommentResponseDto result = commentService.findById(3L);

        // Then
        assertThat(result).isEqualTo(commentResponseDto);
        verify(commentRepository).findById(3L);
        verify(commentMapper).toResponseDto(comment);
    }

    @Test
    void findById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found with id: 999");

        verify(commentRepository).findById(999L);
        verify(commentMapper, never()).toResponseDto(any());
    }

    @Test
    void findByNews_withExistingNewsId_shouldReturnListOfCommentListItemDto() {
        // Given
        List<Comment> comments = Arrays.asList(comment);
        List<CommentListItemDto> commentListItemDtos = Arrays.asList(commentListItemDto);
        
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentRepository.findByNewsOrderByCreationDateDesc(news)).thenReturn(comments);
        when(commentMapper.toListItemDtoList(comments)).thenReturn(commentListItemDtos);

        // When
        List<CommentListItemDto> result = commentService.findByNews(1L);

        // Then
        assertThat(result).isEqualTo(commentListItemDtos);
        verify(newsRepository).findById(1L);
        verify(commentRepository).findByNewsOrderByCreationDateDesc(news);
        verify(commentMapper).toListItemDtoList(comments);
    }

    @Test
    void findByNews_withNonExistingNewsId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.findByNews(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(commentRepository, never()).findByNewsOrderByCreationDateDesc(any());
        verify(commentMapper, never()).toListItemDtoList(any());
    }

    @Test
    void create_withValidData_shouldReturnCreatedCommentResponseDto() {
        // Given
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentComment));
        when(commentMapper.toEntity(commentRequestDto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponseDto(comment)).thenReturn(commentResponseDto);

        // When
        CommentResponseDto result = commentService.create(commentRequestDto);

        // Then
        assertThat(result).isEqualTo(commentResponseDto);
        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(2L);
        verify(commentMapper).toEntity(commentRequestDto);
        verify(commentRepository).save(comment);
        verify(commentMapper).toResponseDto(comment);
    }

    @Test
    void create_withoutParentComment_shouldReturnCreatedCommentResponseDto() {
        // Given
        CommentRequestDto dtoWithoutParent = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(null) // No parent comment
                .build();

        Comment commentWithoutParent = Comment.builder()
                .id(3L)
                .text("Test comment")
                .authorNickname("testuser")
                .news(news)
                .parentComment(null) // No parent comment
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentMapper.toEntity(dtoWithoutParent)).thenReturn(commentWithoutParent);
        when(commentRepository.save(commentWithoutParent)).thenReturn(commentWithoutParent);
        when(commentMapper.toResponseDto(commentWithoutParent)).thenReturn(commentResponseDto);

        // When
        CommentResponseDto result = commentService.create(dtoWithoutParent);

        // Then
        assertThat(result).isEqualTo(commentResponseDto);
        verify(newsRepository).findById(1L);
        verify(commentRepository, never()).findById(anyLong()); // Should not look for parent comment
        verify(commentMapper).toEntity(dtoWithoutParent);
        verify(commentRepository).save(commentWithoutParent);
        verify(commentMapper).toResponseDto(commentWithoutParent);
    }

    @Test
    void create_withNonExistingNewsId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());
        
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(999L) // Non-existing news ID
                .build();

        // When/Then
        assertThatThrownBy(() -> commentService.create(invalidDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(commentRepository, never()).findById(anyLong());
        verify(commentMapper, never()).toEntity(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_withNonExistingParentCommentId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
        
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(999L) // Non-existing parent comment ID
                .build();

        // When/Then
        assertThatThrownBy(() -> commentService.create(invalidDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found with id: 999");

        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(999L);
        verify(commentMapper, never()).toEntity(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void create_withParentCommentFromDifferentNews_shouldThrowIllegalArgumentException() {
        // Given
        News otherNews = News.builder()
                .id(2L)
                .title("Other News")
                .build();

        Comment parentFromOtherNews = Comment.builder()
                .id(2L)
                .text("Parent comment")
                .news(otherNews) // Different news
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentFromOtherNews));
        
        // When/Then
        assertThatThrownBy(() -> commentService.create(commentRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Parent comment does not belong to the specified news");

        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(2L);
        verify(commentMapper, never()).toEntity(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void update_withExistingId_shouldReturnUpdatedCommentResponseDto() {
        // Given
        Comment existingComment = Comment.builder()
                .id(3L)
                .text("Old text")
                .authorNickname("testuser")
                .news(news)
                .build();

        CommentRequestDto updateDto = CommentRequestDto.builder()
                .text("Updated text")
                .authorNickname("newuser") // This should be ignored
                .newsId(2L) // This should be ignored
                .parentCommentId(4L) // This should be ignored
                .build();

        Comment updatedComment = Comment.builder()
                .id(3L)
                .text("Updated text") // Only text is updated
                .authorNickname("testuser") // Other fields remain unchanged
                .news(news)
                .build();

        CommentResponseDto updatedResponseDto = CommentResponseDto.builder()
                .id(3L)
                .text("Updated text")
                .authorNickname("testuser")
                .build();

        when(commentRepository.findById(3L)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(existingComment)).thenReturn(updatedComment);
        when(commentMapper.toResponseDto(updatedComment)).thenReturn(updatedResponseDto);

        // When
        CommentResponseDto result = commentService.update(3L, updateDto);

        // Then
        assertThat(result).isEqualTo(updatedResponseDto);
        verify(commentRepository).findById(3L);
        verify(commentRepository).save(existingComment);
        verify(commentMapper).toResponseDto(updatedComment);
        
        // Verify that only the text was updated
        assertThat(existingComment.getText()).isEqualTo("Updated text");
    }

    @Test
    void update_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.update(999L, commentRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found with id: 999");

        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).save(any());
        verify(commentMapper, never()).toResponseDto(any());
    }

    @Test
    void delete_withExistingId_shouldDeleteComment() {
        // Given
        when(commentRepository.existsById(3L)).thenReturn(true);

        // When
        commentService.delete(3L);

        // Then
        verify(commentRepository).existsById(3L);
        verify(commentRepository).deleteById(3L);
    }

    @Test
    void delete_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> commentService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found with id: 999");

        verify(commentRepository).existsById(999L);
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void findReplies_withExistingParentCommentId_shouldReturnListOfCommentListItemDto() {
        // Given
        List<Comment> replies = Arrays.asList(comment);
        List<CommentListItemDto> replyListItemDtos = Arrays.asList(commentListItemDto);
        
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.findByParentComment(parentComment)).thenReturn(replies);
        when(commentMapper.toListItemDtoList(replies)).thenReturn(replyListItemDtos);

        // When
        List<CommentListItemDto> result = commentService.findReplies(2L);

        // Then
        assertThat(result).isEqualTo(replyListItemDtos);
        verify(commentRepository).findById(2L);
        verify(commentRepository).findByParentComment(parentComment);
        verify(commentMapper).toListItemDtoList(replies);
    }

    @Test
    void findReplies_withNonExistingParentCommentId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.findReplies(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment not found with id: 999");

        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).findByParentComment(any());
        verify(commentMapper, never()).toListItemDtoList(any());
    }
}