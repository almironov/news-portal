package com.dev.news.newsportal.mapper.entity;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
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
class UserEntityMapperTest {

    @Autowired
    private UserEntityMapper userEntityMapper;
    
    private User userEntity;
    private UserModel userModel;
    private News newsEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime creationDate = LocalDateTime.now();

        // Set up news entity (for testing news field handling)
        newsEntity = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .creationDate(creationDate)
                .build();

        // Set up user entity
        userEntity = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .news(Arrays.asList(newsEntity))
                .build();

        // Set up user model
        userModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
    }

    @Test
    void toModel_shouldConvertUserEntityToUserModel() {
        // When
        UserModel result = userEntityMapper.toModel(userEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
        // Note: UserModel doesn't have a news field, so we don't check it
    }

    @Test
    void toModel_withNullEntity_shouldReturnNull() {
        // When
        UserModel result = userEntityMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_withEmptyNews_shouldHandleGracefully() {
        // Given
        User userWithoutNews = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .news(new ArrayList<>())
                .build();

        // When
        UserModel result = userEntityMapper.toModel(userWithoutNews);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void toModel_withNullNews_shouldHandleGracefully() {
        // Given
        User userWithNullNews = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .news(null)
                .build();

        // When
        UserModel result = userEntityMapper.toModel(userWithNullNews);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void toEntity_shouldConvertUserModelToUserEntity() {
        // When
        User result = userEntityMapper.toEntity(userModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getNews()).isEmpty(); // Should be ignored and default to empty list
    }

    @Test
    void toEntity_withNullModel_shouldReturnNull() {
        // When
        User result = userEntityMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldIgnoreNewsField() {
        // Given - UserModel doesn't have news field, but we want to verify the mapping ignores it
        
        // When
        User result = userEntityMapper.toEntity(userModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNews()).isEmpty(); // Should be ignored and default to empty list
    }

    @Test
    void toModelList_shouldConvertListOfUserEntitiesToUserModels() {
        // Given
        User secondUserEntity = User.builder()
                .id(2L)
                .nickname("seconduser")
                .email("second@example.com")
                .role("ADMIN")
                .news(new ArrayList<>())
                .build();
        List<User> userEntities = Arrays.asList(userEntity, secondUserEntity);

        // When
        List<UserModel> result = userEntityMapper.toModelList(userEntities);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getNickname()).isEqualTo("testuser");
        assertThat(result.get(0).getRole()).isEqualTo("USER");
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getNickname()).isEqualTo("seconduser");
        assertThat(result.get(1).getRole()).isEqualTo("ADMIN");
    }

    @Test
    void toModelList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<User> emptyList = Arrays.asList();

        // When
        List<UserModel> result = userEntityMapper.toModelList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toModelList_withNullList_shouldReturnNull() {
        // When
        List<UserModel> result = userEntityMapper.toModelList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntityList_shouldConvertListOfUserModelsToUserEntities() {
        // Given
        UserModel secondUserModel = UserModel.builder()
                .id(2L)
                .nickname("seconduser")
                .email("second@example.com")
                .role("ADMIN")
                .build();
        List<UserModel> userModels = Arrays.asList(userModel, secondUserModel);

        // When
        List<User> result = userEntityMapper.toEntityList(userModels);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getNickname()).isEqualTo("testuser");
        assertThat(result.get(0).getRole()).isEqualTo("USER");
        assertThat(result.get(0).getNews()).isEmpty(); // Should be ignored
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getNickname()).isEqualTo("seconduser");
        assertThat(result.get(1).getRole()).isEqualTo("ADMIN");
        assertThat(result.get(1).getNews()).isEmpty(); // Should be ignored
    }

    @Test
    void toEntityList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<UserModel> emptyList = Arrays.asList();

        // When
        List<User> result = userEntityMapper.toEntityList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toEntityList_withNullList_shouldReturnNull() {
        // When
        List<User> result = userEntityMapper.toEntityList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void bidirectionalMapping_shouldMaintainDataIntegrity() {
        // When - convert entity to model and back to entity
        UserModel convertedModel = userEntityMapper.toModel(userEntity);
        User convertedEntity = userEntityMapper.toEntity(convertedModel);

        // Then - data should be preserved (except news field which is ignored)
        assertThat(convertedEntity.getId()).isEqualTo(userEntity.getId());
        assertThat(convertedEntity.getNickname()).isEqualTo(userEntity.getNickname());
        assertThat(convertedEntity.getEmail()).isEqualTo(userEntity.getEmail());
        assertThat(convertedEntity.getRole()).isEqualTo(userEntity.getRole());
        
        // News field should be empty due to @Mapping(target = "news", ignore = true)
        assertThat(convertedEntity.getNews()).isEmpty();
        // Original entity should still have news
        assertThat(userEntity.getNews()).hasSize(1);
    }

    @Test
    void toModel_withDifferentRoles_shouldHandleAllRoles() {
        // Given
        User adminUser = User.builder()
                .id(2L)
                .nickname("admin")
                .email("admin@example.com")
                .role("ADMIN")
                .news(new ArrayList<>())
                .build();

        User moderatorUser = User.builder()
                .id(3L)
                .nickname("moderator")
                .email("mod@example.com")
                .role("MODERATOR")
                .news(new ArrayList<>())
                .build();

        // When
        UserModel adminResult = userEntityMapper.toModel(adminUser);
        UserModel moderatorResult = userEntityMapper.toModel(moderatorUser);

        // Then
        assertThat(adminResult.getRole()).isEqualTo("ADMIN");
        assertThat(moderatorResult.getRole()).isEqualTo("MODERATOR");
    }

    @Test
    void toEntity_withDifferentRoles_shouldHandleAllRoles() {
        // Given
        UserModel adminModel = UserModel.builder()
                .id(2L)
                .nickname("admin")
                .email("admin@example.com")
                .role("ADMIN")
                .build();

        UserModel moderatorModel = UserModel.builder()
                .id(3L)
                .nickname("moderator")
                .email("mod@example.com")
                .role("MODERATOR")
                .build();

        // When
        User adminResult = userEntityMapper.toEntity(adminModel);
        User moderatorResult = userEntityMapper.toEntity(moderatorModel);

        // Then
        assertThat(adminResult.getRole()).isEqualTo("ADMIN");
        assertThat(moderatorResult.getRole()).isEqualTo("MODERATOR");
        assertThat(adminResult.getNews()).isEmpty();
        assertThat(moderatorResult.getNews()).isEmpty();
    }
}