package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.dto.request.UserRequestDto;
import com.dev.news.newsportal.dto.response.UserResponseDto;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
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
    void getAllUsers_shouldReturnListOfUserResponseDto() throws Exception {
        // Given
        List<UserResponseDto> userList = Arrays.asList(userResponseDto);
        when(userService.findAll()).thenReturn(userList);

        // When/Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nickname", is("testuser")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")))
                .andExpect(jsonPath("$[0].role", is("ROLE_USER")));

        verify(userService).findAll();
    }

    @Test
    void getUserById_withExistingId_shouldReturnUserResponseDto() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(userResponseDto);

        // When/Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(userService).findById(1L);
    }

    @Test
    void getUserById_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(userService.findById(999L)).thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).findById(999L);
    }

    @Test
    void createUser_withValidData_shouldReturnCreatedUserResponseDto() throws Exception {
        // Given
        when(userService.create(any(UserRequestDto.class))).thenReturn(userResponseDto);

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(userService).create(any(UserRequestDto.class));
    }

    @Test
    void createUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        UserRequestDto invalidDto = UserRequestDto.builder()
                .nickname("") // Invalid: nickname is required
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).create(any(UserRequestDto.class));
    }

    @Test
    void createUser_withDuplicateNickname_shouldReturnConflict() throws Exception {
        // Given
        when(userService.create(any(UserRequestDto.class)))
                .thenThrow(new DuplicateResourceException("User", "nickname", "testuser"));

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isConflict());

        verify(userService).create(any(UserRequestDto.class));
    }

    @Test
    void updateUser_withExistingIdAndValidData_shouldReturnUpdatedUserResponseDto() throws Exception {
        // Given
        when(userService.update(eq(1L), any(UserRequestDto.class))).thenReturn(userResponseDto);

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(userService).update(eq(1L), any(UserRequestDto.class));
    }

    @Test
    void updateUser_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(userService.update(eq(999L), any(UserRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // When/Then
        mockMvc.perform(put("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isNotFound());

        verify(userService).update(eq(999L), any(UserRequestDto.class));
    }

    @Test
    void updateUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        UserRequestDto invalidDto = UserRequestDto.builder()
                .nickname("") // Invalid: nickname is required
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).update(anyLong(), any(UserRequestDto.class));
    }

    @Test
    void updateUser_withDuplicateNickname_shouldReturnConflict() throws Exception {
        // Given
        when(userService.update(eq(1L), any(UserRequestDto.class)))
                .thenThrow(new DuplicateResourceException("User", "nickname", "testuser"));

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
                .andExpect(status().isConflict());

        verify(userService).update(eq(1L), any(UserRequestDto.class));
    }

    @Test
    void deleteUser_withExistingId_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(userService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void deleteUser_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("User", "id", 999L))
                .when(userService).delete(999L);

        // When/Then
        mockMvc.perform(delete("/api/v1/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).delete(999L);
    }

    @Test
    void getUserByNickname_withExistingNickname_shouldReturnUserResponseDto() throws Exception {
        // Given
        when(userService.findByNickname("testuser")).thenReturn(userResponseDto);

        // When/Then
        mockMvc.perform(get("/api/v1/users/nickname/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(userService).findByNickname("testuser");
    }

    @Test
    void getUserByNickname_withNonExistingNickname_shouldReturnNotFound() throws Exception {
        // Given
        when(userService.findByNickname("nonexistent"))
                .thenThrow(new ResourceNotFoundException("User", "nickname", "nonexistent"));

        // When/Then
        mockMvc.perform(get("/api/v1/users/nickname/nonexistent"))
                .andExpect(status().isNotFound());

        verify(userService).findByNickname("nonexistent");
    }

    @Test
    void getUserByEmail_withExistingEmail_shouldReturnUserResponseDto() throws Exception {
        // Given
        when(userService.findByEmail("test@example.com")).thenReturn(userResponseDto);

        // When/Then
        mockMvc.perform(get("/api/v1/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));

        verify(userService).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_withNonExistingEmail_shouldReturnNotFound() throws Exception {
        // Given
        when(userService.findByEmail("nonexistent@example.com"))
                .thenThrow(new ResourceNotFoundException("User", "email", "nonexistent@example.com"));

        // When/Then
        mockMvc.perform(get("/api/v1/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(userService).findByEmail("nonexistent@example.com");
    }
}