package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.api.model.users.UserRequest;
import com.dev.news.newsportal.api.model.users.UserResponse;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.api.UserApiMapper;
import com.dev.news.newsportal.model.UserModel;
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

    @MockBean
    private UserApiMapper userApiMapper;

    private UserRequest userRequest;
    private UserResponse userResponse;
    private UserModel userModel;

    @BeforeEach
    void setUp() {
        // Create domain model for service mocking
        userModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        // Create OpenAPI DTOs for request/response
        userRequest = new UserRequest("testuser", "test@example.com", UserRequest.RoleEnum.USER);

        userResponse = new UserResponse()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER");
    }

    @Test
    void getAllUsers_shouldReturnListOfUserResponseDto() throws Exception {
        // Given
        List<UserModel> userModels = Arrays.asList(userModel);
        List<UserResponse> userResponses = Arrays.asList(userResponse);
        when(userService.findAll()).thenReturn(userModels);
        when(userApiMapper.toResponseList(userModels)).thenReturn(userResponses);

        // When/Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].nickname", is("testuser")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")))
                .andExpect(jsonPath("$[0].role", is("USER")));

        verify(userService).findAll();
        verify(userApiMapper).toResponseList(userModels);
    }

    @Test
    void getUserById_withExistingId_shouldReturnUserResponseDto() throws Exception {
        // Given
        when(userService.findById(1L)).thenReturn(userModel);
        when(userApiMapper.toResponse(userModel)).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userService).findById(1L);
        verify(userApiMapper).toResponse(userModel);
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
        when(userApiMapper.toModel(any(UserRequest.class))).thenReturn(userModel);
        when(userService.create(any(UserModel.class))).thenReturn(userModel);
        when(userApiMapper.toResponse(userModel)).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userApiMapper).toModel(any(UserRequest.class));
        verify(userService).create(any(UserModel.class));
        verify(userApiMapper).toResponse(userModel);
    }

    @Test
    void createUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        UserRequest invalidRequest = new UserRequest("", "test@example.com", UserRequest.RoleEnum.USER); // Invalid: nickname is empty

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).create(any(UserModel.class));
    }

    @Test
    void createUser_withDuplicateNickname_shouldReturnConflict() throws Exception {
        // Given
        when(userApiMapper.toModel(any(UserRequest.class))).thenReturn(userModel);
        when(userService.create(any(UserModel.class)))
                .thenThrow(new DuplicateResourceException("User", "nickname", "testuser"));

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict());

        verify(userApiMapper).toModel(any(UserRequest.class));
        verify(userService).create(any(UserModel.class));
    }

    @Test
    void updateUser_withExistingIdAndValidData_shouldReturnUpdatedUserResponseDto() throws Exception {
        // Given
        when(userApiMapper.toModel(any(UserRequest.class))).thenReturn(userModel);
        when(userService.update(eq(1L), any(UserModel.class))).thenReturn(userModel);
        when(userApiMapper.toResponse(userModel)).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userApiMapper).toModel(any(UserRequest.class));
        verify(userService).update(eq(1L), any(UserModel.class));
        verify(userApiMapper).toResponse(userModel);
    }

    @Test
    void updateUser_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(userApiMapper.toModel(any(UserRequest.class))).thenReturn(userModel);
        when(userService.update(eq(999L), any(UserModel.class)))
                .thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // When/Then
        mockMvc.perform(put("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound());

        verify(userApiMapper).toModel(any(UserRequest.class));
        verify(userService).update(eq(999L), any(UserModel.class));
    }

    @Test
    void updateUser_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        UserRequest invalidRequest = new UserRequest("", "test@example.com", UserRequest.RoleEnum.USER); // Invalid: nickname is empty

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, times(0)).update(anyLong(), any(UserModel.class));
    }

    @Test
    void updateUser_withDuplicateNickname_shouldReturnConflict() throws Exception {
        // Given
        when(userApiMapper.toModel(any(UserRequest.class))).thenReturn(userModel);
        when(userService.update(eq(1L), any(UserModel.class)))
                .thenThrow(new DuplicateResourceException("User", "nickname", "testuser"));

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict());

        verify(userApiMapper).toModel(any(UserRequest.class));
        verify(userService).update(eq(1L), any(UserModel.class));
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
        when(userService.findByNickname("testuser")).thenReturn(userModel);
        when(userApiMapper.toResponse(userModel)).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/users/nickname/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userService).findByNickname("testuser");
        verify(userApiMapper).toResponse(userModel);
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
        when(userService.findByEmail("test@example.com")).thenReturn(userModel);
        when(userApiMapper.toResponse(userModel)).thenReturn(userResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nickname", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(userService).findByEmail("test@example.com");
        verify(userApiMapper).toResponse(userModel);
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