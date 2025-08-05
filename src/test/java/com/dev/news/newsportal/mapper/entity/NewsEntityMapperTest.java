package com.dev.news.newsportal.mapper.entity;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
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
class NewsEntityMapperTest {

    @Autowired
    private NewsEntityMapper newsEntityMapper;
    
    private News newsEntity;
    private NewsModel newsModel;
    private User userEntity;
    private UserModel userModel;
    private Comment commentEntity;
    private CommentModel commentModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();

        // Set up user entity and model
        userEntity = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        userModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        // Set up comment entity and model
        commentEntity = Comment.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate.minusHours(1))
                .build();

        commentModel = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate.minusHours(1))
                .newsId(1L)
                .replies(new ArrayList<>())
                .build();

        // Set up news entity
        newsEntity = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(userEntity)
                .comments(Arrays.asList(commentEntity))
                .build();

        // Set up news model
        newsModel = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(userModel)
                .comments(Arrays.asList(commentModel))
                .build();
    }

    @Test
    void toModel_shouldConvertNewsEntityToNewsModel() {
        // When
        NewsModel result = newsEntityMapper.toModel(newsEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test News");
        assertThat(result.getText()).isEqualTo("This is a test news article");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(result.getCreationDate()).isEqualTo(creationDate);
        
        // Check author mapping
        assertThat(result.getAuthor()).isNotNull();
        assertThat(result.getAuthor().getId()).isEqualTo(1L);
        assertThat(result.getAuthor().getNickname()).isEqualTo("testuser");
        assertThat(result.getAuthor().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getAuthor().getRole()).isEqualTo("USER");
        
        // Check comments mapping
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getId()).isEqualTo(1L);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Test comment");
        assertThat(result.getComments().get(0).getAuthorNickname()).isEqualTo("testuser");
    }

    @Test
    void toModel_withNullEntity_shouldReturnNull() {
        // When
        NewsModel result = newsEntityMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_withNullAuthor_shouldHandleGracefully() {
        // Given
        News newsWithoutAuthor = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .creationDate(creationDate)
                .author(null)
                .comments(new ArrayList<>())
                .build();

        // When
        NewsModel result = newsEntityMapper.toModel(newsWithoutAuthor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthor()).isNull();
    }

    @Test
    void toModel_withEmptyComments_shouldHandleGracefully() {
        // Given
        News newsWithoutComments = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .creationDate(creationDate)
                .author(userEntity)
                .comments(new ArrayList<>())
                .build();

        // When
        NewsModel result = newsEntityMapper.toModel(newsWithoutComments);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void toEntity_shouldConvertNewsModelToNewsEntity() {
        // When
        News result = newsEntityMapper.toEntity(newsModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test News");
        assertThat(result.getText()).isEqualTo("This is a test news article");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(result.getCreationDate()).isEqualTo(creationDate);
        
        // Check author mapping
        assertThat(result.getAuthor()).isNotNull();
        assertThat(result.getAuthor().getId()).isEqualTo(1L);
        assertThat(result.getAuthor().getNickname()).isEqualTo("testuser");
        assertThat(result.getAuthor().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getAuthor().getRole()).isEqualTo("USER");
        
        // Check comments mapping
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getId()).isEqualTo(1L);
        assertThat(result.getComments().get(0).getText()).isEqualTo("Test comment");
        assertThat(result.getComments().get(0).getAuthorNickname()).isEqualTo("testuser");
    }

    @Test
    void toEntity_withNullModel_shouldReturnNull() {
        // When
        News result = newsEntityMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_withNullAuthor_shouldHandleGracefully() {
        // Given
        NewsModel newsModelWithoutAuthor = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .creationDate(creationDate)
                .author(null)
                .comments(new ArrayList<>())
                .build();

        // When
        News result = newsEntityMapper.toEntity(newsModelWithoutAuthor);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAuthor()).isNull();
    }

    @Test
    void toEntity_withEmptyComments_shouldHandleGracefully() {
        // Given
        NewsModel newsModelWithoutComments = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .creationDate(creationDate)
                .author(userModel)
                .comments(new ArrayList<>())
                .build();

        // When
        News result = newsEntityMapper.toEntity(newsModelWithoutComments);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void toModelList_shouldConvertListOfNewsEntitiesToNewsModels() {
        // Given
        News secondNewsEntity = News.builder()
                .id(2L)
                .title("Second News")
                .text("This is the second news article")
                .creationDate(creationDate.minusDays(1))
                .author(userEntity)
                .comments(new ArrayList<>())
                .build();
        List<News> newsEntities = Arrays.asList(newsEntity, secondNewsEntity);

        // When
        List<NewsModel> result = newsEntityMapper.toModelList(newsEntities);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test News");
        assertThat(result.get(0).getComments()).hasSize(1);
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Second News");
        assertThat(result.get(1).getComments()).isEmpty();
    }

    @Test
    void toModelList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<News> emptyList = Arrays.asList();

        // When
        List<NewsModel> result = newsEntityMapper.toModelList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toModelList_withNullList_shouldReturnNull() {
        // When
        List<NewsModel> result = newsEntityMapper.toModelList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntityList_shouldConvertListOfNewsModelsToNewsEntities() {
        // Given
        NewsModel secondNewsModel = NewsModel.builder()
                .id(2L)
                .title("Second News")
                .text("This is the second news article")
                .creationDate(creationDate.minusDays(1))
                .author(userModel)
                .comments(new ArrayList<>())
                .build();
        List<NewsModel> newsModels = Arrays.asList(newsModel, secondNewsModel);

        // When
        List<News> result = newsEntityMapper.toEntityList(newsModels);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test News");
        assertThat(result.get(0).getComments()).hasSize(1);
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Second News");
        assertThat(result.get(1).getComments()).isEmpty();
    }

    @Test
    void toEntityList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<NewsModel> emptyList = Arrays.asList();

        // When
        List<News> result = newsEntityMapper.toEntityList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toEntityList_withNullList_shouldReturnNull() {
        // When
        List<News> result = newsEntityMapper.toEntityList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void bidirectionalMapping_shouldMaintainDataIntegrity() {
        // When - convert entity to model and back to entity
        NewsModel convertedModel = newsEntityMapper.toModel(newsEntity);
        News convertedEntity = newsEntityMapper.toEntity(convertedModel);

        // Then - data should be preserved
        assertThat(convertedEntity.getId()).isEqualTo(newsEntity.getId());
        assertThat(convertedEntity.getTitle()).isEqualTo(newsEntity.getTitle());
        assertThat(convertedEntity.getText()).isEqualTo(newsEntity.getText());
        assertThat(convertedEntity.getImageUrl()).isEqualTo(newsEntity.getImageUrl());
        assertThat(convertedEntity.getCreationDate()).isEqualTo(newsEntity.getCreationDate());
        
        // Check author preservation
        assertThat(convertedEntity.getAuthor().getId()).isEqualTo(newsEntity.getAuthor().getId());
        assertThat(convertedEntity.getAuthor().getNickname()).isEqualTo(newsEntity.getAuthor().getNickname());
        
        // Check comments preservation
        assertThat(convertedEntity.getComments()).hasSize(newsEntity.getComments().size());
        assertThat(convertedEntity.getComments().get(0).getId()).isEqualTo(newsEntity.getComments().get(0).getId());
    }
}