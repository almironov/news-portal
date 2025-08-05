package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.api.model.comments.CommentListItem;
import com.dev.news.newsportal.api.model.comments.CommentRequest;
import com.dev.news.newsportal.api.model.comments.CommentResponse;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.api.CommentApiMapper;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CommentApiMapper commentApiMapper;

    private CommentRequest commentRequest;
    private CommentResponse commentResponse;
    private CommentListItem commentListItem;
    private CommentModel commentModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();
        
        // Create domain model for service mocking
        commentModel = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(null)
                .replies(new ArrayList<>())
                .build();

        // Create OpenAPI DTOs for request/response
        commentRequest = new CommentRequest("Test comment", "testuser", 1L)
                .parentCommentId(null);

        commentResponse = new CommentResponse()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate.atOffset(java.time.ZoneOffset.UTC))
                .replies(new ArrayList<>());

        commentListItem = new CommentListItem()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate.atOffset(java.time.ZoneOffset.UTC))
                .hasReplies(false);
    }

    @Test
    void getCommentById_withExistingId_shouldReturnCommentResponseDto() throws Exception {
        // Given
        when(commentService.findById(1L)).thenReturn(commentModel);
        when(commentApiMapper.toResponse(commentModel)).thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorNickname", is("testuser")))
                .andExpect(jsonPath("$.replies", hasSize(0)));

        verify(commentService).findById(1L);
        verify(commentApiMapper).toResponse(commentModel);
    }

    @Test
    void getCommentById_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.findById(999L)).thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService).findById(999L);
    }

    @Test
    void getCommentsByNews_withExistingNewsId_shouldReturnListOfCommentListItemDto() throws Exception {
        // Given
        List<CommentModel> commentModels = Arrays.asList(commentModel);
        List<CommentListItem> commentListItems = Arrays.asList(commentListItem);
        when(commentService.findByNews(1L)).thenReturn(commentModels);
        when(commentApiMapper.toListItemList(commentModels)).thenReturn(commentListItems);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].text", is("Test comment")))
                .andExpect(jsonPath("$[0].authorNickname", is("testuser")))
                .andExpect(jsonPath("$[0].hasReplies", is(false)));

        verify(commentService).findByNews(1L);
        verify(commentApiMapper).toListItemList(commentModels);
    }

    @Test
    void getCommentsByNews_withNonExistingNewsId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.findByNews(999L)).thenThrow(new ResourceNotFoundException("News", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/comments/news/999"))
                .andExpect(status().isNotFound());

        verify(commentService).findByNews(999L);
    }

    @Test
    void createComment_withValidData_shouldReturnCreatedCommentResponseDto() throws Exception {
        // Given
        when(commentApiMapper.toModel(any(CommentRequest.class))).thenReturn(commentModel);
        when(commentService.create(any(CommentModel.class))).thenReturn(commentModel);
        when(commentApiMapper.toResponse(commentModel)).thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/comments/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorNickname", is("testuser")))
                .andExpect(jsonPath("$.replies", hasSize(0)));

        verify(commentApiMapper).toModel(any(CommentRequest.class));
        verify(commentService).create(any(CommentModel.class));
        verify(commentApiMapper).toResponse(commentModel);
    }

    @Test
    void createComment_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        CommentRequest invalidRequest = new CommentRequest("", "testuser", 1L); // Invalid: text is empty

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commentService, times(0)).create(any(CommentModel.class));
    }

    @Test
    void createComment_withNonExistingNewsId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentApiMapper.toModel(any(CommentRequest.class))).thenReturn(commentModel);
        when(commentService.create(any(CommentModel.class)))
                .thenThrow(new ResourceNotFoundException("News", "id", 999L));

        CommentRequest requestWithNonExistingNewsId = new CommentRequest("Test comment", "testuser", 999L);

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithNonExistingNewsId)))
                .andExpect(status().isNotFound());

        verify(commentApiMapper).toModel(any(CommentRequest.class));
        verify(commentService).create(any(CommentModel.class));
    }

    @Test
    void createComment_withNonExistingParentCommentId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentApiMapper.toModel(any(CommentRequest.class))).thenReturn(commentModel);
        when(commentService.create(any(CommentModel.class)))
                .thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        CommentRequest requestWithNonExistingParentId = new CommentRequest("Test comment", "testuser", 1L)
                .parentCommentId(999L);

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithNonExistingParentId)))
                .andExpect(status().isNotFound());

        verify(commentApiMapper).toModel(any(CommentRequest.class));
        verify(commentService).create(any(CommentModel.class));
    }

    @Test
    void updateComment_withExistingIdAndValidData_shouldReturnUpdatedCommentResponseDto() throws Exception {
        // Given
        when(commentApiMapper.toModel(any(CommentRequest.class))).thenReturn(commentModel);
        when(commentService.update(eq(1L), any(CommentModel.class))).thenReturn(commentModel);
        when(commentApiMapper.toResponse(commentModel)).thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorNickname", is("testuser")))
                .andExpect(jsonPath("$.replies", hasSize(0)));

        verify(commentApiMapper).toModel(any(CommentRequest.class));
        verify(commentService).update(eq(1L), any(CommentModel.class));
        verify(commentApiMapper).toResponse(commentModel);
    }

    @Test
    void updateComment_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentApiMapper.toModel(any(CommentRequest.class))).thenReturn(commentModel);
        when(commentService.update(eq(999L), any(CommentModel.class)))
                .thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // When/Then
        mockMvc.perform(put("/api/v1/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isNotFound());

        verify(commentApiMapper).toModel(any(CommentRequest.class));
        verify(commentService).update(eq(999L), any(CommentModel.class));
    }

    @Test
    void updateComment_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        CommentRequest invalidRequest = new CommentRequest("", "testuser", 1L); // Invalid: text is empty

        // When/Then
        mockMvc.perform(put("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commentService, times(0)).update(anyLong(), any(CommentModel.class));
    }

    @Test
    void deleteComment_withExistingId_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(commentService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService).delete(1L);
    }

    @Test
    void deleteComment_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Comment", "id", 999L))
                .when(commentService).delete(999L);

        // When/Then
        mockMvc.perform(delete("/api/v1/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService).delete(999L);
    }

    @Test
    void getCommentReplies_withExistingCommentId_shouldReturnListOfCommentListItemDto() throws Exception {
        // Given
        List<CommentModel> replyModels = Arrays.asList(commentModel);
        List<CommentListItem> replyListItems = Arrays.asList(commentListItem);
        when(commentService.findReplies(1L)).thenReturn(replyModels);
        when(commentApiMapper.toListItemList(replyModels)).thenReturn(replyListItems);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].text", is("Test comment")))
                .andExpect(jsonPath("$[0].authorNickname", is("testuser")))
                .andExpect(jsonPath("$[0].hasReplies", is(false)));

        verify(commentService).findReplies(1L);
        verify(commentApiMapper).toListItemList(replyModels);
    }

    @Test
    void getCommentReplies_withNonExistingCommentId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.findReplies(999L)).thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/comments/999/replies"))
                .andExpect(status().isNotFound());

        verify(commentService).findReplies(999L);
    }

    @Test
    void getCommentReplies_withNoReplies_shouldReturnEmptyList() throws Exception {
        // Given
        when(commentService.findReplies(1L)).thenReturn(Arrays.asList());
        when(commentApiMapper.toListItemList(Arrays.asList())).thenReturn(Arrays.asList());

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(commentService).findReplies(1L);
        verify(commentApiMapper).toListItemList(Arrays.asList());
    }
}