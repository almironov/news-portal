package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.CommentEntityMapper;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.repository.CommentRepository;
import com.dev.news.newsportal.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private CommentEntityMapper commentEntityMapper;

    private CommentServiceImpl commentService;

    private News newsEntity;
    private Comment commentEntity;
    private Comment parentCommentEntity;
    private CommentModel commentModel;
    private CommentModel parentCommentModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        // Reset all mocks to ensure test isolation
        MockitoAnnotations.openMocks(this);
        
        // Manually instantiate service with mocked dependencies
        commentService = new CommentServiceImpl(commentRepository, newsRepository, commentEntityMapper);
        
        creationDate = LocalDateTime.now();
        
        // Set up entity data
        newsEntity = News.builder()
                .id(1L)
                .title("Test News")
                .text("Test news content")
                .build();

        parentCommentEntity = Comment.builder()
                .id(2L)
                .text("Parent comment")
                .authorNickname("parentuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .replies(new ArrayList<>())
                .build();

        commentEntity = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        // Set up domain model data
        parentCommentModel = CommentModel.builder()
                .id(2L)
                .text("Parent comment")
                .authorNickname("parentuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        commentModel = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();
    }

    @Test
    void findById_withExistingId_shouldReturnCommentModel() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(commentEntity));
        when(commentEntityMapper.toModel(commentEntity)).thenReturn(commentModel);

        // When
        CommentModel result = commentService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("testuser");
        assertThat(result.getNewsId()).isEqualTo(1L);

        verify(commentRepository).findById(1L);
        verify(commentEntityMapper).toModel(commentEntity);
    }

    @Test
    void findById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");

        verify(commentRepository).findById(999L);
        verify(commentEntityMapper, never()).toModel(any(Comment.class));
    }

    @Test
    void findByNews_withExistingNewsId_shouldReturnListOfCommentModels() {
        // Given
        List<Comment> commentEntities = Arrays.asList(commentEntity);
        List<CommentModel> commentModels = Arrays.asList(commentModel);
        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(commentRepository.findByNewsOrderByCreationDateDesc(newsEntity)).thenReturn(commentEntities);
        when(commentEntityMapper.toModelList(commentEntities)).thenReturn(commentModels);

        // When
        List<CommentModel> result = commentService.findByNews(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");

        verify(newsRepository).findById(1L);
        verify(commentRepository).findByNewsOrderByCreationDateDesc(newsEntity);
        verify(commentEntityMapper).toModelList(commentEntities);
    }

    @Test
    void findByNews_withNonExistingNewsId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.findByNews(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(commentRepository, never()).findByNewsOrderByCreationDateDesc(any(News.class));
    }

    @Test
    void create_withValidCommentModel_shouldReturnCreatedCommentModel() {
        // Given
        CommentModel inputModel = CommentModel.builder()
                .text("New comment")
                .authorNickname("newuser")
                .newsId(1L)
                .parentCommentId(null)
                .build();

        Comment inputEntity = Comment.builder()
                .text("New comment")
                .authorNickname("newuser")
                .news(newsEntity)
                .parentComment(null)
                .build();

        Comment savedEntity = Comment.builder()
                .id(3L)
                .text("New comment")
                .authorNickname("newuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        CommentModel savedModel = CommentModel.builder()
                .id(3L)
                .text("New comment")
                .authorNickname("newuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(commentEntityMapper.toEntity(inputModel)).thenReturn(inputEntity);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedEntity);
        when(commentEntityMapper.toModel(savedEntity)).thenReturn(savedModel);

        // When
        CommentModel result = commentService.create(inputModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getText()).isEqualTo("New comment");
        assertThat(result.getAuthorNickname()).isEqualTo("newuser");

        verify(newsRepository).findById(1L);
        verify(commentEntityMapper).toEntity(inputModel);
        verify(commentRepository).save(any(Comment.class));
        verify(commentEntityMapper).toModel(savedEntity);
    }

    @Test
    void create_withNonExistingNews_shouldThrowResourceNotFoundException() {
        // Given
        CommentModel inputModel = CommentModel.builder()
                .text("New comment")
                .authorNickname("newuser")
                .newsId(999L)
                .build();

        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.create(inputModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void create_withValidParentComment_shouldReturnCreatedCommentModel() {
        // Given
        CommentModel inputModel = CommentModel.builder()
                .text("Reply comment")
                .authorNickname("replyuser")
                .newsId(1L)
                .parentCommentId(2L)
                .build();

        Comment inputEntity = Comment.builder()
                .text("Reply comment")
                .authorNickname("replyuser")
                .news(newsEntity)
                .parentComment(parentCommentEntity)
                .build();

        Comment savedEntity = Comment.builder()
                .id(4L)
                .text("Reply comment")
                .authorNickname("replyuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .parentComment(parentCommentEntity)
                .replies(new ArrayList<>())
                .build();

        CommentModel savedModel = CommentModel.builder()
                .id(4L)
                .text("Reply comment")
                .authorNickname("replyuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(2L)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentCommentEntity));
        when(commentEntityMapper.toEntity(inputModel)).thenReturn(inputEntity);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedEntity);
        when(commentEntityMapper.toModel(savedEntity)).thenReturn(savedModel);

        // When
        CommentModel result = commentService.create(inputModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getText()).isEqualTo("Reply comment");
        assertThat(result.getParentCommentId()).isEqualTo(2L);

        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(2L);
        verify(commentEntityMapper).toEntity(inputModel);
        verify(commentRepository).save(any(Comment.class));
        verify(commentEntityMapper).toModel(savedEntity);
    }

    @Test
    void create_withNonExistingParentComment_shouldThrowResourceNotFoundException() {
        // Given
        CommentModel inputModel = CommentModel.builder()
                .text("Reply comment")
                .authorNickname("replyuser")
                .newsId(1L)
                .parentCommentId(999L)
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.create(inputModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");

        verify(newsRepository).findById(1L);
        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void update_withExistingIdAndValidData_shouldReturnUpdatedCommentModel() {
        // Given
        CommentModel updateModel = CommentModel.builder()
                .text("Updated comment")
                .authorNickname("testuser")
                .newsId(1L)
                .build();

        Comment updatedEntity = Comment.builder()
                .id(1L)
                .text("Updated comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .replies(new ArrayList<>())
                .build();

        CommentModel updatedModel = CommentModel.builder()
                .id(1L)
                .text("Updated comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .replies(new ArrayList<>())
                .build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(commentEntity));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedEntity);
        when(commentEntityMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        // When
        CommentModel result = commentService.update(1L, updateModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Updated comment");

        verify(commentRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
        verify(commentEntityMapper).toModel(updatedEntity);
    }

    @Test
    void update_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        CommentModel updateModel = CommentModel.builder()
                .text("Updated comment")
                .build();

        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.update(999L, updateModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");

        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void delete_withExistingId_shouldDeleteComment() {
        // Given
        when(commentRepository.existsById(1L)).thenReturn(true);

        // When
        commentService.delete(1L);

        // Then
        verify(commentRepository).existsById(1L);
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void delete_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> commentService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");

        verify(commentRepository).existsById(999L);
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void findReplies_withExistingCommentId_shouldReturnListOfCommentModels() {
        // Given
        List<Comment> replyEntities = Arrays.asList(commentEntity);
        List<CommentModel> replyModels = Arrays.asList(commentModel);
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentCommentEntity));
        when(commentRepository.findByParentComment(parentCommentEntity)).thenReturn(replyEntities);
        when(commentEntityMapper.toModelList(replyEntities)).thenReturn(replyModels);

        // When
        List<CommentModel> result = commentService.findReplies(2L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");

        verify(commentRepository).findById(2L);
        verify(commentRepository).findByParentComment(parentCommentEntity);
        verify(commentEntityMapper).toModelList(replyEntities);
    }

    @Test
    void findReplies_withNonExistingCommentId_shouldThrowResourceNotFoundException() {
        // Given
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> commentService.findReplies(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id: 999");

        verify(commentRepository).findById(999L);
        verify(commentRepository, never()).findByParentComment(any(Comment.class));
    }

    @Test
    void findReplies_withNoReplies_shouldReturnEmptyList() {
        // Given
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentCommentEntity));
        when(commentRepository.findByParentComment(parentCommentEntity)).thenReturn(Arrays.asList());
        when(commentEntityMapper.toModelList(Arrays.asList())).thenReturn(Arrays.asList());

        // When
        List<CommentModel> result = commentService.findReplies(2L);

        // Then
        assertThat(result).isEmpty();

        verify(commentRepository).findById(2L);
        verify(commentRepository).findByParentComment(parentCommentEntity);
        verify(commentEntityMapper).toModelList(Arrays.asList());
    }
}