package com.dev.news.newsportal.mapper.entity;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.model.CommentModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CommentEntityMapperTest {

    @Autowired
    private CommentEntityMapper commentEntityMapper;
    
    private Comment commentEntity;
    private CommentModel commentModel;
    private News newsEntity;
    private User userEntity;
    private Comment parentCommentEntity;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();

        // Set up user entity
        userEntity = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        // Set up news entity
        newsEntity = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .creationDate(creationDate)
                .author(userEntity)
                .build();

        // Set up parent comment entity
        parentCommentEntity = Comment.builder()
                .id(2L)
                .text("Parent comment")
                .authorNickname("parentuser")
                .creationDate(creationDate.minusHours(1))
                .news(newsEntity)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        // Set up comment entity
        commentEntity = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .parentComment(parentCommentEntity)
                .replies(new ArrayList<>())
                .build();

        // Set up comment model
        commentModel = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(2L)
                .replies(new ArrayList<>())
                .build();
    }

    @Test
    void toModel_shouldConvertCommentEntityToCommentModel() {
        // When
        CommentModel result = commentEntityMapper.toModel(commentEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("testuser");
        assertThat(result.getCreationDate()).isEqualTo(creationDate);
        assertThat(result.getNewsId()).isEqualTo(1L); // Mapped from news.id
        assertThat(result.getParentCommentId()).isEqualTo(2L); // Mapped from parentComment.id
        assertThat(result.getReplies()).isEmpty();
    }

    @Test
    void toModel_withNullEntity_shouldReturnNull() {
        // When
        CommentModel result = commentEntityMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_withNullNews_shouldHandleGracefully() {
        // Given
        Comment commentWithoutNews = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .news(null)
                .parentComment(parentCommentEntity)
                .replies(new ArrayList<>())
                .build();

        // When
        CommentModel result = commentEntityMapper.toModel(commentWithoutNews);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNewsId()).isNull();
        assertThat(result.getParentCommentId()).isEqualTo(2L);
    }

    @Test
    void toModel_withNullParentComment_shouldHandleGracefully() {
        // Given
        Comment commentWithoutParent = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        // When
        CommentModel result = commentEntityMapper.toModel(commentWithoutParent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNewsId()).isEqualTo(1L);
        assertThat(result.getParentCommentId()).isNull();
    }

    @Test
    void toEntity_shouldConvertCommentModelToCommentEntity() {
        // When
        Comment result = commentEntityMapper.toEntity(commentModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("testuser");
        assertThat(result.getCreationDate()).isEqualTo(creationDate);
        assertThat(result.getReplies()).isEmpty();
        
        // Check news reference creation
        assertThat(result.getNews()).isNotNull();
        assertThat(result.getNews().getId()).isEqualTo(1L);
        
        // Check parent comment reference creation
        assertThat(result.getParentComment()).isNotNull();
        assertThat(result.getParentComment().getId()).isEqualTo(2L);
    }

    @Test
    void toEntity_withNullModel_shouldReturnNull() {
        // When
        Comment result = commentEntityMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_withNullNewsId_shouldHandleGracefully() {
        // Given
        CommentModel commentModelWithoutNewsId = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(null)
                .parentCommentId(2L)
                .replies(new ArrayList<>())
                .build();

        // When
        Comment result = commentEntityMapper.toEntity(commentModelWithoutNewsId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNews()).isNull();
        assertThat(result.getParentComment()).isNotNull();
        assertThat(result.getParentComment().getId()).isEqualTo(2L);
    }

    @Test
    void toEntity_withNullParentCommentId_shouldHandleGracefully() {
        // Given
        CommentModel commentModelWithoutParentId = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        // When
        Comment result = commentEntityMapper.toEntity(commentModelWithoutParentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNews()).isNotNull();
        assertThat(result.getNews().getId()).isEqualTo(1L);
        assertThat(result.getParentComment()).isNull();
    }

    @Test
    void toModelList_shouldConvertListOfCommentEntitiesToCommentModels() {
        // Given
        Comment secondCommentEntity = Comment.builder()
                .id(3L)
                .text("Second comment")
                .authorNickname("seconduser")
                .creationDate(creationDate.minusMinutes(30))
                .news(newsEntity)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();
        List<Comment> commentEntities = Arrays.asList(commentEntity, secondCommentEntity);

        // When
        List<CommentModel> result = commentEntityMapper.toModelList(commentEntities);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
        assertThat(result.get(0).getNewsId()).isEqualTo(1L);
        assertThat(result.get(0).getParentCommentId()).isEqualTo(2L);
        
        assertThat(result.get(1).getId()).isEqualTo(3L);
        assertThat(result.get(1).getText()).isEqualTo("Second comment");
        assertThat(result.get(1).getNewsId()).isEqualTo(1L);
        assertThat(result.get(1).getParentCommentId()).isNull();
    }

    @Test
    void toModelList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<Comment> emptyList = Arrays.asList();

        // When
        List<CommentModel> result = commentEntityMapper.toModelList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toModelList_withNullList_shouldReturnNull() {
        // When
        List<CommentModel> result = commentEntityMapper.toModelList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntityList_shouldConvertListOfCommentModelsToCommentEntities() {
        // Given
        CommentModel secondCommentModel = CommentModel.builder()
                .id(3L)
                .text("Second comment")
                .authorNickname("seconduser")
                .creationDate(creationDate.minusMinutes(30))
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();
        List<CommentModel> commentModels = Arrays.asList(commentModel, secondCommentModel);

        // When
        List<Comment> result = commentEntityMapper.toEntityList(commentModels);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
        assertThat(result.get(0).getNews().getId()).isEqualTo(1L);
        assertThat(result.get(0).getParentComment().getId()).isEqualTo(2L);
        
        assertThat(result.get(1).getId()).isEqualTo(3L);
        assertThat(result.get(1).getText()).isEqualTo("Second comment");
        assertThat(result.get(1).getNews().getId()).isEqualTo(1L);
        assertThat(result.get(1).getParentComment()).isNull();
    }

    @Test
    void toEntityList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<CommentModel> emptyList = Arrays.asList();

        // When
        List<Comment> result = commentEntityMapper.toEntityList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toEntityList_withNullList_shouldReturnNull() {
        // When
        List<Comment> result = commentEntityMapper.toEntityList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void createNewsReference_shouldCreateNewsWithId() {
        // When
        News result = commentEntityMapper.createNewsReference(5L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isNull(); // Only ID should be set
        assertThat(result.getText()).isNull();
    }

    @Test
    void createNewsReference_withNullId_shouldReturnNull() {
        // When
        News result = commentEntityMapper.createNewsReference(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void createCommentReference_shouldCreateCommentWithId() {
        // When
        Comment result = commentEntityMapper.createCommentReference(10L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getText()).isNull(); // Only ID should be set
        assertThat(result.getAuthorNickname()).isNull();
    }

    @Test
    void createCommentReference_withNullId_shouldReturnNull() {
        // When
        Comment result = commentEntityMapper.createCommentReference(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void bidirectionalMapping_shouldMaintainDataIntegrity() {
        // When - convert entity to model and back to entity
        CommentModel convertedModel = commentEntityMapper.toModel(commentEntity);
        Comment convertedEntity = commentEntityMapper.toEntity(convertedModel);

        // Then - data should be preserved
        assertThat(convertedEntity.getId()).isEqualTo(commentEntity.getId());
        assertThat(convertedEntity.getText()).isEqualTo(commentEntity.getText());
        assertThat(convertedEntity.getAuthorNickname()).isEqualTo(commentEntity.getAuthorNickname());
        assertThat(convertedEntity.getCreationDate()).isEqualTo(commentEntity.getCreationDate());
        
        // Check reference preservation
        assertThat(convertedEntity.getNews().getId()).isEqualTo(commentEntity.getNews().getId());
        assertThat(convertedEntity.getParentComment().getId()).isEqualTo(commentEntity.getParentComment().getId());
    }

    @Test
    void bidirectionalMapping_withNullReferences_shouldMaintainDataIntegrity() {
        // Given - comment without parent
        Comment commentWithoutParent = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .news(newsEntity)
                .parentComment(null)
                .replies(new ArrayList<>())
                .build();

        // When - convert entity to model and back to entity
        CommentModel convertedModel = commentEntityMapper.toModel(commentWithoutParent);
        Comment convertedEntity = commentEntityMapper.toEntity(convertedModel);

        // Then - data should be preserved
        assertThat(convertedEntity.getId()).isEqualTo(commentWithoutParent.getId());
        assertThat(convertedEntity.getText()).isEqualTo(commentWithoutParent.getText());
        assertThat(convertedEntity.getNews().getId()).isEqualTo(commentWithoutParent.getNews().getId());
        assertThat(convertedEntity.getParentComment()).isNull();
    }
}