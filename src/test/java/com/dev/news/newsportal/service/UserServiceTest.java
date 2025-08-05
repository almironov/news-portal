package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.UserModel;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEntityMapper userEntityMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User userEntity;
    private UserModel userModel;

    @BeforeEach
    void setUp() {
        // Set up entity data
        userEntity = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        // Set up domain model data
        userModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();
    }

    @Test
    void findById_withExistingId_shouldReturnUserModel() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userEntityMapper.toModel(userEntity)).thenReturn(userModel);

        // When
        UserModel result = userService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo("USER");

        verify(userRepository).findById(1L);
        verify(userEntityMapper).toModel(userEntity);
    }

    @Test
    void findById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userEntityMapper, never()).toModel(any(User.class));
    }

    @Test
    void findAll_shouldReturnListOfUserModels() {
        // Given
        List<User> userEntities = Arrays.asList(userEntity);
        List<UserModel> userModels = Arrays.asList(userModel);
        when(userRepository.findAll()).thenReturn(userEntities);
        when(userEntityMapper.toModelList(userEntities)).thenReturn(userModels);

        // When
        List<UserModel> result = userService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getNickname()).isEqualTo("testuser");

        verify(userRepository).findAll();
        verify(userEntityMapper).toModelList(userEntities);
    }

    @Test
    void create_withValidUserModel_shouldReturnCreatedUserModel() {
        // Given
        UserModel inputModel = UserModel.builder()
                .nickname("newuser")
                .email("newuser@example.com")
                .role("USER")
                .build();

        User inputEntity = User.builder()
                .nickname("newuser")
                .email("newuser@example.com")
                .role("USER")
                .build();

        User savedEntity = User.builder()
                .id(2L)
                .nickname("newuser")
                .email("newuser@example.com")
                .role("USER")
                .build();

        UserModel savedModel = UserModel.builder()
                .id(2L)
                .nickname("newuser")
                .email("newuser@example.com")
                .role("USER")
                .build();

        when(userRepository.existsByNickname("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userEntityMapper.toEntity(inputModel)).thenReturn(inputEntity);
        when(userRepository.save(any(User.class))).thenReturn(savedEntity);
        when(userEntityMapper.toModel(savedEntity)).thenReturn(savedModel);

        // When
        UserModel result = userService.create(inputModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getNickname()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");

        verify(userRepository).existsByNickname("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userEntityMapper).toEntity(inputModel);
        verify(userRepository).save(any(User.class));
        verify(userEntityMapper).toModel(savedEntity);
    }

    @Test
    void create_withDuplicateNickname_shouldThrowDuplicateResourceException() {
        // Given
        UserModel inputModel = UserModel.builder()
                .nickname("testuser")
                .email("newemail@example.com")
                .role("USER")
                .build();

        when(userRepository.existsByNickname("testuser")).thenReturn(true);
        when(userRepository.findByNickname("testuser")).thenReturn(Optional.of(userEntity));

        // When/Then
        assertThatThrownBy(() -> userService.create(inputModel))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User with nickname: testuser already exists");

        verify(userRepository).existsByNickname("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_withDuplicateEmail_shouldThrowDuplicateResourceException() {
        // Given
        UserModel inputModel = UserModel.builder()
                .nickname("newuser")
                .email("test@example.com")
                .role("USER")
                .build();

        when(userRepository.existsByNickname("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));

        // When/Then
        assertThatThrownBy(() -> userService.create(inputModel))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User with email: test@example.com already exists");

        verify(userRepository).existsByNickname("newuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_withExistingIdAndValidData_shouldReturnUpdatedUserModel() {
        // Given
        UserModel updateModel = UserModel.builder()
                .nickname("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        User updatedEntity = User.builder()
                .id(1L)
                .nickname("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        UserModel updatedModel = UserModel.builder()
                .id(1L)
                .nickname("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.existsByNickname("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedEntity);
        when(userEntityMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        // When
        UserModel result = userService.update(1L, updateModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNickname()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getRole()).isEqualTo("ADMIN");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userEntityMapper).toModel(updatedEntity);
    }

    @Test
    void update_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        UserModel updateModel = UserModel.builder()
                .nickname("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.update(999L, updateModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_withDuplicateNickname_shouldThrowDuplicateResourceException() {
        // Given
        UserModel updateModel = UserModel.builder()
                .nickname("existinguser")
                .email("updated@example.com")
                .role("ADMIN")
                .build();

        User existingUserWithNickname = User.builder()
                .id(2L)
                .nickname("existinguser")
                .email("existing@example.com")
                .role("USER")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userRepository.existsByNickname("existinguser")).thenReturn(true);
        when(userRepository.findByNickname("existinguser")).thenReturn(Optional.of(existingUserWithNickname));

        // When/Then
        assertThatThrownBy(() -> userService.update(1L, updateModel))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("User with nickname: existinguser already exists");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
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
                .hasMessage("User not found with id: 999");

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByNickname_withExistingNickname_shouldReturnUserModel() {
        // Given
        when(userRepository.findByNickname("testuser")).thenReturn(Optional.of(userEntity));
        when(userEntityMapper.toModel(userEntity)).thenReturn(userModel);

        // When
        UserModel result = userService.findByNickname("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo("testuser");

        verify(userRepository).findByNickname("testuser");
        verify(userEntityMapper).toModel(userEntity);
    }

    @Test
    void findByNickname_withNonExistingNickname_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findByNickname("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findByNickname("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with nickname: nonexistent");

        verify(userRepository).findByNickname("nonexistent");
        verify(userEntityMapper, never()).toModel(any(User.class));
    }

    @Test
    void findByEmail_withExistingEmail_shouldReturnUserModel() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(userEntityMapper.toModel(userEntity)).thenReturn(userModel);

        // When
        UserModel result = userService.findByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
        verify(userEntityMapper).toModel(userEntity);
    }

    @Test
    void findByEmail_withNonExistingEmail_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.findByEmail("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with email: nonexistent@example.com");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userEntityMapper, never()).toModel(any(User.class));
    }
}