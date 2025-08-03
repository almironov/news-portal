package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
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

    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;
    private CommentListItemDto commentListItemDto;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();
        
        commentRequestDto = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(null)
                .build();

        commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .replies(new ArrayList<>())
                .build();

        commentListItemDto = CommentListItemDto.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .hasReplies(false)
                .build();
    }

    @Test
    void getCommentById_withExistingId_shouldReturnCommentResponseDto() throws Exception {
        // Given
        when(commentService.findById(1L)).thenReturn(commentResponseDto);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorNickname", is("testuser")))
                .andExpect(jsonPath("$.replies", hasSize(0)));

        verify(commentService).findById(1L);
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
        List<CommentListItemDto> commentList = Arrays.asList(commentListItemDto);
        when(commentService.findByNews(1L)).thenReturn(commentList);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].text", is("Test comment")))
                .andExpect(jsonPath("$[0].authorNickname", is("testuser")))
                .andExpect(jsonPath("$[0].hasReplies", is(false)));

        verify(commentService).findByNews(1L);
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
        when(commentService.create(any(CommentRequestDto.class))).thenReturn(commentResponseDto);

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/comments/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorNickname", is("testuser")))
                .andExpect(jsonPath("$.replies", hasSize(0)));

        verify(commentService).create(any(CommentRequestDto.class));
    }

    @Test
    void createComment_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text("") // Invalid: text is required
                .authorNickname("testuser")
                .newsId(1L)
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(commentService, times(0)).create(any(CommentRequestDto.class));
    }

    @Test
    void createComment_withNonExistingNewsId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.create(any(CommentRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("News", "id", 999L));

        CommentRequestDto dtoWithNonExistingNewsId = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(999L) // Non-existing news ID
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoWithNonExistingNewsId)))
                .andExpect(status().isNotFound());

        verify(commentService).create(any(CommentRequestDto.class));
    }

    @Test
    void createComment_withNonExistingParentCommentId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.create(any(CommentRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        CommentRequestDto dtoWithNonExistingParentId = CommentRequestDto.builder()
                .text("Test comment")
                .authorNickname("testuser")
                .newsId(1L)
                .parentCommentId(999L) // Non-existing parent comment ID
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoWithNonExistingParentId)))
                .andExpect(status().isNotFound());

        verify(commentService).create(any(CommentRequestDto.class));
    }

    @Test
    void updateComment_withExistingIdAndValidData_shouldReturnUpdatedCommentResponseDto() throws Exception {
        // Given
        when(commentService.update(eq(1L), any(CommentRequestDto.class))).thenReturn(commentResponseDto);

        // When/Then
        mockMvc.perform(put("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.authorNickname", is("testuser")))
                .andExpect(jsonPath("$.replies", hasSize(0)));

        verify(commentService).update(eq(1L), any(CommentRequestDto.class));
    }

    @Test
    void updateComment_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.update(eq(999L), any(CommentRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // When/Then
        mockMvc.perform(put("/api/v1/comments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isNotFound());

        verify(commentService).update(eq(999L), any(CommentRequestDto.class));
    }

    @Test
    void updateComment_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        CommentRequestDto invalidDto = CommentRequestDto.builder()
                .text("") // Invalid: text is required
                .authorNickname("testuser")
                .newsId(1L)
                .build();

        // When/Then
        mockMvc.perform(put("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(commentService, times(0)).update(anyLong(), any(CommentRequestDto.class));
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
    void getCommentReplies_withExistingId_shouldReturnListOfCommentListItemDto() throws Exception {
        // Given
        List<CommentListItemDto> replyList = Arrays.asList(commentListItemDto);
        when(commentService.findReplies(1L)).thenReturn(replyList);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].text", is("Test comment")))
                .andExpect(jsonPath("$[0].authorNickname", is("testuser")))
                .andExpect(jsonPath("$[0].hasReplies", is(false)));

        verify(commentService).findReplies(1L);
    }

    @Test
    void getCommentReplies_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(commentService.findReplies(999L)).thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/comments/999/replies"))
                .andExpect(status().isNotFound());

        verify(commentService).findReplies(999L);
    }
}