package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.UserRequestDto;
import com.dev.news.newsportal.dto.response.UserResponseDto;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.UserMapper;
import com.dev.news.newsportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        // Set up test data
        user = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        userRequestDto = UserRequestDto.builder()
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();
    }

    @Test
    void findById_withExistingId_shouldReturnUserResponseDto() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.findById(1L);

        // Then
        assertThat(result).isEqualTo(userResponseDto);
        verify(userRepository).findById(1L);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void findById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).toResponseDto(any());
    }

    @Test
    void findAll_shouldReturnListOfUserResponseDto() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .nickname("anotheruser")
                .email("another@example.com")
                .role("ROLE_ADMIN")
                .build();

        UserResponseDto userResponseDto2 = UserResponseDto.builder()
                .id(2L)
                .nickname("anotheruser")
                .email("another@example.com")
                .role("ROLE_ADMIN")
                .build();

        List<User> users = Arrays.asList(user, user2);
        
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);
        when(userMapper.toResponseDto(user2)).thenReturn(userResponseDto2);

        // When
        List<UserResponseDto> result = userService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(userResponseDto, userResponseDto2);
        verify(userRepository).findAll();
        verify(userMapper, times(2)).toResponseDto(any(User.class));
    }

    @Test
    void create_withUniqueFields_shouldReturnCreatedUserResponseDto() {
        // Given
        when(userRepository.existsByNickname(userRequestDto.getNickname())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDto.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userRequestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.create(userRequestDto);

        // Then
        assertThat(result).isEqualTo(userResponseDto);
        verify(userRepository).existsByNickname(userRequestDto.getNickname());
        verify(userRepository).existsByEmail(userRequestDto.getEmail());
        verify(userMapper).toEntity(userRequestDto);
        verify(userRepository).save(user);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void create_withDuplicateNickname_shouldThrowDuplicateResourceException() {
        // Given
        when(userRepository.existsByNickname(userRequestDto.getNickname())).thenReturn(true);
        when(userRepository.findByNickname(userRequestDto.getNickname())).thenReturn(Optional.of(user));

        // When/Then
        assertThatThrownBy(() -> userService.create(userRequestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User with nickname: testuser already exists");

        verify(userRepository).existsByNickname(userRequestDto.getNickname());
        verify(userRepository).findByNickname(userRequestDto.getNickname());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_withDuplicateEmail_shouldThrowDuplicateResourceException() {
        // Given
        when(userRepository.existsByNickname(userRequestDto.getNickname())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDto.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.of(user));

        // When/Then
        assertThatThrownBy(() -> userService.create(userRequestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User with email: test@example.com already exists");

        verify(userRepository).existsByNickname(userRequestDto.getNickname());
        verify(userRepository).existsByEmail(userRequestDto.getEmail());
        verify(userRepository).findByEmail(userRequestDto.getEmail());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_withExistingIdAndUniqueFields_shouldReturnUpdatedUserResponseDto() {
        // Given
        User existingUser = User.builder()
                .id(1L)
                .nickname("oldnickname")
                .email("old@example.com")
                .role("ROLE_USER")
                .build();

        UserRequestDto updateDto = UserRequestDto.builder()
                .nickname("newnickname")
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .nickname("newnickname")
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        UserResponseDto updatedResponseDto = UserResponseDto.builder()
                .id(1L)
                .nickname("newnickname")
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByNickname(updateDto.getNickname())).thenReturn(false);
        when(userRepository.existsByEmail(updateDto.getEmail())).thenReturn(false);
        doNothing().when(userMapper).updateEntityFromDto(updateDto, existingUser);
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toResponseDto(updatedUser)).thenReturn(updatedResponseDto);

        // When
        UserResponseDto result = userService.update(1L, updateDto);

        // Then
        assertThat(result).isEqualTo(updatedResponseDto);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByNickname(updateDto.getNickname());
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userMapper).updateEntityFromDto(updateDto, existingUser);
        verify(userRepository).save(existingUser);
        verify(userMapper).toResponseDto(updatedUser);
    }

    @Test
    void update_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.update(999L, userRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).updateEntityFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_withDuplicateNickname_shouldThrowDuplicateResourceException() {
        // Given
        User existingUser = User.builder()
                .id(1L)
                .nickname("oldnickname")
                .email("old@example.com")
                .role("ROLE_USER")
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .nickname("newnickname")
                .email("another@example.com")
                .role("ROLE_USER")
                .build();

        UserRequestDto updateDto = UserRequestDto.builder()
                .nickname("newnickname")
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByNickname(updateDto.getNickname())).thenReturn(true);
        when(userRepository.findByNickname(updateDto.getNickname())).thenReturn(Optional.of(anotherUser));

        // When/Then
        assertThatThrownBy(() -> userService.update(1L, updateDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("User with nickname: newnickname already exists");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByNickname(updateDto.getNickname());
        verify(userRepository).findByNickname(updateDto.getNickname());
        verify(userMapper, never()).updateEntityFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_withExistingId_shouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.delete(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByNickname_withExistingNickname_shouldReturnUserResponseDto() {
        // Given
        when(userRepository.findByNickname("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.findByNickname("testuser");

        // Then
        assertThat(result).isEqualTo(userResponseDto);
        verify(userRepository).findByNickname("testuser");
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void findByNickname_withNonExistingNickname_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findByNickname("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findByNickname("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with nickname: nonexistent");

        verify(userRepository).findByNickname("nonexistent");
        verify(userMapper, never()).toResponseDto(any());
    }

    @Test
    void findByEmail_withExistingEmail_shouldReturnUserResponseDto() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.findByEmail("test@example.com");

        // Then
        assertThat(result).isEqualTo(userResponseDto);
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toResponseDto(user);
    }

    @Test
    void findByEmail_withNonExistingEmail_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findByEmail("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userMapper, never()).toResponseDto(any());
    }
}