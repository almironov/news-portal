package com.dev.news.newsportal.mapper.api;

import com.dev.news.newsportal.api.model.news.UserSummary;
import com.dev.news.newsportal.api.model.users.UserRequest;
import com.dev.news.newsportal.api.model.users.UserResponse;
import com.dev.news.newsportal.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserApiMapperTest {

    @Autowired
    private UserApiMapper userApiMapper;
    
    private UserModel userModel;

    @BeforeEach
    void setUp() {
        userModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
    }

    @Test
    void toModel_shouldConvertUserRequestToUserModel() {
        // Given
        UserRequest userRequest = new UserRequest("testuser", "test@example.com", UserRequest.RoleEnum.USER);

        // When
        UserModel result = userApiMapper.toModel(userRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // Should be ignored
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void toModel_withAdminRole_shouldConvertCorrectly() {
        // Given
        UserRequest userRequest = new UserRequest("adminuser", "admin@example.com", UserRequest.RoleEnum.ADMIN);

        // When
        UserModel result = userApiMapper.toModel(userRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("adminuser");
        assertThat(result.getEmail()).isEqualTo("admin@example.com");
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void toModel_withModeratorRole_shouldConvertCorrectly() {
        // Given
        UserRequest userRequest = new UserRequest("moderator", "mod@example.com", UserRequest.RoleEnum.MODERATOR);

        // When
        UserModel result = userApiMapper.toModel(userRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("moderator");
        assertThat(result.getEmail()).isEqualTo("mod@example.com");
        assertThat(result.getRole()).isEqualTo("MODERATOR");
    }

    @Test
    void toModel_withNullRole_shouldHandleGracefully() {
        // Given
        UserRequest userRequest = new UserRequest("testuser", "test@example.com", null);

        // When
        UserModel result = userApiMapper.toModel(userRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isNull();
    }

    @Test
    void toResponse_shouldConvertUserModelToUserResponse() {
        // When
        UserResponse result = userApiMapper.toResponse(userModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");
    }

    @Test
    void toResponse_withNullUserModel_shouldReturnNull() {
        // When
        UserResponse result = userApiMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toSummary_shouldConvertUserModelToUserSummary() {
        // When
        UserSummary result = userApiMapper.toSummary(userModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
    }

    @Test
    void toSummary_withNullUserModel_shouldReturnNull() {
        // When
        UserSummary result = userApiMapper.toSummary(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toResponseList_shouldConvertListOfUserModelsToUserResponses() {
        // Given
        UserModel secondUser = UserModel.builder()
                .id(2L)
                .nickname("seconduser")
                .email("second@example.com")
                .role("ADMIN")
                .build();
        List<UserModel> userModels = Arrays.asList(userModel, secondUser);

        // When
        List<UserResponse> result = userApiMapper.toResponseList(userModels);

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
    void toResponseList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<UserModel> emptyList = Arrays.asList();

        // When
        List<UserResponse> result = userApiMapper.toResponseList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toResponseList_withNullList_shouldReturnNull() {
        // When
        List<UserResponse> result = userApiMapper.toResponseList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toSummaryList_shouldConvertListOfUserModelsToUserSummaries() {
        // Given
        UserModel secondUser = UserModel.builder()
                .id(2L)
                .nickname("seconduser")
                .email("second@example.com")
                .role("ADMIN")
                .build();
        List<UserModel> userModels = Arrays.asList(userModel, secondUser);

        // When
        List<UserSummary> result = userApiMapper.toSummaryList(userModels);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getNickname()).isEqualTo("testuser");
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getNickname()).isEqualTo("seconduser");
    }

    @Test
    void toSummaryList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<UserModel> emptyList = Arrays.asList();

        // When
        List<UserSummary> result = userApiMapper.toSummaryList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toSummaryList_withNullList_shouldReturnNull() {
        // When
        List<UserSummary> result = userApiMapper.toSummaryList(null);

        // Then
        assertThat(result).isNull();
    }
}