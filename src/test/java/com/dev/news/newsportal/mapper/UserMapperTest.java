package com.dev.news.newsportal.mapper;

import com.dev.news.newsportal.dto.request.UserRequestDto;
import com.dev.news.newsportal.dto.response.UserResponseDto;
import com.dev.news.newsportal.dto.response.UserSummaryDto;
import com.dev.news.newsportal.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toEntity_shouldMapDtoToEntity() {
        // Given
        UserRequestDto dto = UserRequestDto.builder()
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        // When
        User user = userMapper.toEntity(dto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull(); // ID should be ignored in mapping
        assertThat(user.getNickname()).isEqualTo(dto.getNickname());
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
        assertThat(user.getRole()).isEqualTo(dto.getRole());
        assertThat(user.getNews()).isNotNull(); // Should be initialized as empty list
        assertThat(user.getNews()).isEmpty();
    }

    @Test
    void toResponseDto_shouldMapEntityToDto() {
        // Given
        User user = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .news(new ArrayList<>())
                .build();

        // When
        UserResponseDto dto = userMapper.toResponseDto(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getNickname()).isEqualTo(user.getNickname());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getRole()).isEqualTo(user.getRole());
    }

    @Test
    void toSummaryDto_shouldMapEntityToSummaryDto() {
        // Given
        User user = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .news(new ArrayList<>())
                .build();

        // When
        UserSummaryDto dto = userMapper.toSummaryDto(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getNickname()).isEqualTo(user.getNickname());
        // Email and role should not be included in summary DTO
    }

    @Test
    void updateEntityFromDto_shouldUpdateEntityWithDtoValues() {
        // Given
        User existingUser = User.builder()
                .id(1L)
                .nickname("oldnickname")
                .email("old@example.com")
                .role("ROLE_USER")
                .news(new ArrayList<>())
                .build();

        UserRequestDto dto = UserRequestDto.builder()
                .nickname("newnickname")
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        // When
        userMapper.updateEntityFromDto(dto, existingUser);

        // Then
        assertThat(existingUser.getId()).isEqualTo(1L); // ID should not be changed
        assertThat(existingUser.getNickname()).isEqualTo(dto.getNickname());
        assertThat(existingUser.getEmail()).isEqualTo(dto.getEmail());
        assertThat(existingUser.getRole()).isEqualTo(dto.getRole());
        assertThat(existingUser.getNews()).isNotNull(); // News should not be changed
        assertThat(existingUser.getNews()).isEmpty();
    }
}