package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.event.CommentCreatedApplicationEvent;
import com.dev.news.newsportal.mapper.entity.CommentEntityMapper;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.repository.CommentRepository;
import com.dev.news.newsportal.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for CommentServiceImpl with event publishing.
 * Tests the complete flow from service method calls to application event publishing.
 */
@ExtendWith(MockitoExtension.class)
@RecordApplicationEvents
class CommentServiceImplIntegrationTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private CommentEntityMapper commentEntityMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CommentServiceImpl commentService;

    private User author;
    private News news;
    private Comment parentComment;
    private Comment comment;
    private CommentModel commentModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, newsRepository, commentEntityMapper, eventPublisher);
        
        creationDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        
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
                .creationDate(creationDate.minusHours(2))
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
                .id(null) // New entity
                .text("This is a test comment")
                .creationDate(creationDate)
                .authorNickname("commentuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        // Set up domain model data
        commentModel = CommentModel.builder()
                .id(null) // New entity
                .text("This is a test comment")
                .authorNickname("commentuser")
                .newsId(100L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();
    }

    @Test
    void create_shouldPublishCommentCreatedApplicationEvent() {
        // Given
        Comment savedComment = Comment.builder()
                .id(200L)
                .text("This is a test comment")
                .creationDate(creationDate)
                .authorNickname("commentuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        CommentModel returnedModel = CommentModel.builder()
                .id(200L)
                .text("This is a test comment")
                .creationDate(creationDate)
                .authorNickname("commentuser")
                .newsId(100L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(100L)).thenReturn(Optional.of(news));
        when(commentEntityMapper.toEntity(commentModel)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentEntityMapper.toModel(savedComment)).thenReturn(returnedModel);

        // When
        CommentModel result = commentService.create(commentModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getText()).isEqualTo("This is a test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("commentuser");
        assertThat(result.getNewsId()).isEqualTo(100L);

        // Verify that application event was published
        ArgumentCaptor<CommentCreatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(CommentCreatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentCreatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.getComment()).isEqualTo(savedComment);
        assertThat(publishedEvent.getSource()).isEqualTo(commentService);

        // Verify repository interactions
        verify(newsRepository).findById(100L);
        verify(commentRepository).save(any(Comment.class));
        verify(commentEntityMapper).toEntity(commentModel);
        verify(commentEntityMapper).toModel(savedComment);
    }

    @Test
    void create_withParentComment_shouldPublishEventWithParentInfo() {
        // Given
        CommentModel replyModel = CommentModel.builder()
                .text("This is a reply comment")
                .authorNickname("replyuser")
                .newsId(100L)
                .parentCommentId(50L)
                .replies(new ArrayList<>())
                .build();

        Comment replyComment = Comment.builder()
                .text("This is a reply comment")
                .creationDate(creationDate)
                .authorNickname("replyuser")
                .news(news)
                .parentComment(parentComment)
                .replies(new ArrayList<>())
                .build();

        Comment savedReply = Comment.builder()
                .id(300L)
                .text("This is a reply comment")
                .creationDate(creationDate)
                .authorNickname("replyuser")
                .news(news)
                .parentComment(parentComment)
                .replies(new ArrayList<>())
                .build();

        CommentModel returnedReplyModel = CommentModel.builder()
                .id(300L)
                .text("This is a reply comment")
                .creationDate(creationDate)
                .authorNickname("replyuser")
                .newsId(100L)
                .parentCommentId(50L)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(100L)).thenReturn(Optional.of(news));
        when(commentRepository.findById(50L)).thenReturn(Optional.of(parentComment));
        when(commentEntityMapper.toEntity(replyModel)).thenReturn(replyComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReply);
        when(commentEntityMapper.toModel(savedReply)).thenReturn(returnedReplyModel);

        // When
        CommentModel result = commentService.create(replyModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(300L);
        assertThat(result.getParentCommentId()).isEqualTo(50L);

        // Verify that application event was published with parent comment info
        ArgumentCaptor<CommentCreatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(CommentCreatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentCreatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getComment()).isEqualTo(savedReply);
        assertThat(publishedEvent.getComment().getParentComment()).isEqualTo(parentComment);
        assertThat(publishedEvent.getComment().getNews()).isEqualTo(news);

        // Verify repository interactions
        verify(newsRepository).findById(100L);
        verify(commentRepository).findById(50L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_withLongText_shouldPublishEventCorrectly() {
        // Given
        String longText = "This is a very long comment text that contains multiple sentences and paragraphs. " +
                "It should be properly handled by the service and the event should contain the complete text. " +
                "The comment creation process should work correctly regardless of the text length.";

        CommentModel longCommentModel = CommentModel.builder()
                .text(longText)
                .authorNickname("longtextuser")
                .newsId(100L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        Comment longComment = Comment.builder()
                .text(longText)
                .creationDate(creationDate)
                .authorNickname("longtextuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        Comment savedLongComment = Comment.builder()
                .id(400L)
                .text(longText)
                .creationDate(creationDate)
                .authorNickname("longtextuser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(100L)).thenReturn(Optional.of(news));
        when(commentEntityMapper.toEntity(longCommentModel)).thenReturn(longComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedLongComment);
        when(commentEntityMapper.toModel(savedLongComment)).thenReturn(longCommentModel);

        // When
        commentService.create(longCommentModel);

        // Then
        ArgumentCaptor<CommentCreatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(CommentCreatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentCreatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getComment().getText()).isEqualTo(longText);
        assertThat(publishedEvent.getComment().getAuthorNickname()).isEqualTo("longtextuser");
    }

    @Test
    void create_multipleComments_shouldPublishMultipleEvents() {
        // Given
        Comment savedComment1 = Comment.builder().id(201L).text("Comment 1")
                .creationDate(creationDate).authorNickname("user1").news(news)
                .parentComment(null).replies(new ArrayList<>()).build();
        Comment savedComment2 = Comment.builder().id(202L).text("Comment 2")
                .creationDate(creationDate).authorNickname("user2").news(news)
                .parentComment(null).replies(new ArrayList<>()).build();

        CommentModel returnedModel1 = CommentModel.builder().id(201L).text("Comment 1")
                .creationDate(creationDate).authorNickname("user1").newsId(100L)
                .parentCommentId(null).replies(new ArrayList<>()).build();
        CommentModel returnedModel2 = CommentModel.builder().id(202L).text("Comment 2")
                .creationDate(creationDate).authorNickname("user2").newsId(100L)
                .parentCommentId(null).replies(new ArrayList<>()).build();

        when(newsRepository.findById(100L)).thenReturn(Optional.of(news));
        when(commentEntityMapper.toEntity(any(CommentModel.class))).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment1, savedComment2);
        when(commentEntityMapper.toModel(savedComment1)).thenReturn(returnedModel1);
        when(commentEntityMapper.toModel(savedComment2)).thenReturn(returnedModel2);

        // When
        commentService.create(commentModel);
        commentService.create(commentModel);

        // Then
        verify(eventPublisher, times(2)).publishEvent(any(CommentCreatedApplicationEvent.class));
        verify(commentRepository, times(2)).save(any(Comment.class));
    }

    @Test
    void update_shouldNotPublishAnyEvent() {
        // Given
        Long commentId = 200L;
        Comment existingComment = Comment.builder()
                .id(commentId)
                .text("Original comment")
                .creationDate(creationDate)
                .authorNickname("originaluser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        CommentModel updateModel = CommentModel.builder()
                .id(commentId)
                .text("Updated comment")
                .authorNickname("originaluser")
                .newsId(100L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        Comment updatedComment = Comment.builder()
                .id(commentId)
                .text("Updated comment")
                .creationDate(creationDate)
                .authorNickname("originaluser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
        when(commentEntityMapper.toModel(updatedComment)).thenReturn(updateModel);

        // When
        commentService.update(commentId, updateModel);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void delete_shouldNotPublishAnyEvent() {
        // Given
        Long commentId = 200L;
        when(commentRepository.existsById(commentId)).thenReturn(true);

        // When
        commentService.delete(commentId);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
        verify(commentRepository).existsById(commentId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void findById_shouldNotPublishAnyEvent() {
        // Given
        Comment existingComment = Comment.builder()
                .id(200L)
                .text("Existing comment")
                .creationDate(creationDate)
                .authorNickname("existinguser")
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        when(commentRepository.findById(200L)).thenReturn(Optional.of(existingComment));
        when(commentEntityMapper.toModel(existingComment)).thenReturn(commentModel);

        // When
        commentService.findById(200L);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
        verify(commentRepository).findById(200L);
    }

    @Test
    void create_withSpecialCharacters_shouldPublishEventCorrectly() {
        // Given
        String specialText = "Comment with special chars: @#$%^&*()_+{}|:<>?[]\\;'\",./ and émojis 😀🎉";
        String specialNickname = "user@domain.com";

        CommentModel specialCommentModel = CommentModel.builder()
                .text(specialText)
                .authorNickname(specialNickname)
                .newsId(100L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        Comment specialComment = Comment.builder()
                .text(specialText)
                .creationDate(creationDate)
                .authorNickname(specialNickname)
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        Comment savedSpecialComment = Comment.builder()
                .id(500L)
                .text(specialText)
                .creationDate(creationDate)
                .authorNickname(specialNickname)
                .news(news)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        when(newsRepository.findById(100L)).thenReturn(Optional.of(news));
        when(commentEntityMapper.toEntity(specialCommentModel)).thenReturn(specialComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedSpecialComment);
        when(commentEntityMapper.toModel(savedSpecialComment)).thenReturn(specialCommentModel);

        // When
        commentService.create(specialCommentModel);

        // Then
        ArgumentCaptor<CommentCreatedApplicationEvent> eventCaptor = 
                ArgumentCaptor.forClass(CommentCreatedApplicationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        CommentCreatedApplicationEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getComment().getText()).isEqualTo(specialText);
        assertThat(publishedEvent.getComment().getAuthorNickname()).isEqualTo(specialNickname);
    }
}