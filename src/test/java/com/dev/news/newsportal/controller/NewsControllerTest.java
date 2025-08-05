package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.api.model.news.*;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.api.NewsApiMapper;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.service.NewsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NewsService newsService;

    @MockitoBean
    private NewsApiMapper newsApiMapper;

    private NewsRequest newsRequest;
    private NewsResponse newsResponse;
    private NewsListItem newsListItem;
    private UserSummary authorSummary;
    private NewsModel newsModel;
    private UserModel authorModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();
        
        // Create domain models for service mocking
        authorModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        newsModel = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(authorModel)
                .comments(Arrays.asList())
                .build();

        // Create OpenAPI DTOs for request/response
        authorSummary = new UserSummary()
                .id(1L)
                .nickname("testuser");

        newsRequest = new NewsRequest("Test News", "This is a test news article", 1L)
                .imageUrl(java.net.URI.create("https://example.com/image.jpg"));

        newsResponse = new NewsResponse()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl(java.net.URI.create("https://example.com/image.jpg"))
                .creationDate(creationDate.atOffset(java.time.ZoneOffset.UTC))
                .author(authorSummary)
                .commentCount(0L);

        newsListItem = new NewsListItem()
                .id(1L)
                .title("Test News")
                .imageUrl(java.net.URI.create("https://example.com/image.jpg"))
                .creationDate(creationDate.atOffset(java.time.ZoneOffset.UTC))
                .author(authorSummary)
                .commentCount(0L);
    }

    @Test
    void getAllNews_withDefaultPagination_shouldReturnPagedResponse() throws Exception {
        // Given
        Pageable defaultPageable = PageRequest.of(0, 10);
        Page<NewsModel> newsPage = new PageImpl<>(Arrays.asList(newsModel), defaultPageable, 1);
        PagedNewsListResponse pagedResponse = new PagedNewsListResponse()
                .content(Arrays.asList(newsListItem))
                .totalElements(1L)
                .totalPages(1)
                .size(10)
                .number(0)
                .numberOfElements(1)
                .first(true)
                .last(true)
                .empty(false);
        
        when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);
        when(newsApiMapper.toPagedResponse(newsPage)).thenReturn(pagedResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Test News")))
                .andExpect(jsonPath("$.content[0].imageUrl", is("https://example.com/image.jpg")))
                .andExpect(jsonPath("$.content[0].author.id", is(1)))
                .andExpect(jsonPath("$.content[0].author.nickname", is("testuser")))
                .andExpect(jsonPath("$.content[0].commentCount", is(0)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(true)));

        verify(newsService).findAll(any(Pageable.class));
        verify(newsApiMapper).toPagedResponse(newsPage);
    }

    @Test
    void getNewsById_withExistingId_shouldReturnNewsResponseDto() throws Exception {
        // Given
        when(newsService.findById(1L)).thenReturn(newsModel);
        when(newsApiMapper.toResponse(newsModel)).thenReturn(newsResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test News")))
                .andExpect(jsonPath("$.text", is("This is a test news article")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/image.jpg")))
                .andExpect(jsonPath("$.author.id", is(1)))
                .andExpect(jsonPath("$.author.nickname", is("testuser")))
                .andExpect(jsonPath("$.commentCount", is(0)));

        verify(newsService).findById(1L);
        verify(newsApiMapper).toResponse(newsModel);
    }

    @Test
    void getNewsById_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(newsService.findById(999L)).thenThrow(new ResourceNotFoundException("News", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/news/999"))
                .andExpect(status().isNotFound());

        verify(newsService).findById(999L);
    }

    @Test
    void createNews_withValidData_shouldReturnCreatedNewsResponseDto() throws Exception {
        // Given
        when(newsApiMapper.toModel(any(NewsRequest.class))).thenReturn(newsModel);
        when(newsService.create(any(NewsModel.class))).thenReturn(newsModel);
        when(newsApiMapper.toResponse(newsModel)).thenReturn(newsResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newsRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/news/1"))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test News")))
                .andExpect(jsonPath("$.text", is("This is a test news article")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/image.jpg")))
                .andExpect(jsonPath("$.author.id", is(1)))
                .andExpect(jsonPath("$.author.nickname", is("testuser")))
                .andExpect(jsonPath("$.commentCount", is(0)));

        verify(newsApiMapper).toModel(any(NewsRequest.class));
        verify(newsService).create(any(NewsModel.class));
        verify(newsApiMapper).toResponse(newsModel);
    }

    @Test
    void createNews_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        NewsRequest invalidRequest = new NewsRequest("", "This is a test news article", 1L); // Invalid: title is empty

        // When/Then
        mockMvc.perform(post("/api/v1/news")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(newsService, times(0)).create(any(NewsModel.class));
    }

    @Test
    void updateNews_withExistingIdAndValidData_shouldReturnUpdatedNewsResponseDto() throws Exception {
        // Given
        when(newsApiMapper.toModel(any(NewsRequest.class))).thenReturn(newsModel);
        when(newsService.update(eq(1L), any(NewsModel.class))).thenReturn(newsModel);
        when(newsApiMapper.toResponse(newsModel)).thenReturn(newsResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/news/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test News")))
                .andExpect(jsonPath("$.text", is("This is a test news article")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/image.jpg")))
                .andExpect(jsonPath("$.author.id", is(1)))
                .andExpect(jsonPath("$.author.nickname", is("testuser")))
                .andExpect(jsonPath("$.commentCount", is(0)));

        verify(newsApiMapper).toModel(any(NewsRequest.class));
        verify(newsService).update(eq(1L), any(NewsModel.class));
        verify(newsApiMapper).toResponse(newsModel);
    }

    @Test
    void updateNews_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        when(newsApiMapper.toModel(any(NewsRequest.class))).thenReturn(newsModel);
        when(newsService.update(eq(999L), any(NewsModel.class)))
                .thenThrow(new ResourceNotFoundException("News", "id", 999L));

        // When/Then
        mockMvc.perform(put("/api/v1/news/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newsRequest)))
                .andExpect(status().isNotFound());

        verify(newsApiMapper).toModel(any(NewsRequest.class));
        verify(newsService).update(eq(999L), any(NewsModel.class));
    }

    @Test
    void updateNews_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        NewsRequest invalidRequest = new NewsRequest("", "This is a test news article", 1L); // Invalid: title is empty

        // When/Then
        mockMvc.perform(put("/api/v1/news/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(newsService, times(0)).update(anyLong(), any(NewsModel.class));
    }

    @Test
    void deleteNews_withExistingId_shouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(newsService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/news/1"))
                .andExpect(status().isNoContent());

        verify(newsService).delete(1L);
    }

    @Test
    void deleteNews_withNonExistingId_shouldReturnNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("News", "id", 999L))
                .when(newsService).delete(999L);

        // When/Then
        mockMvc.perform(delete("/api/v1/news/999"))
                .andExpect(status().isNotFound());

        verify(newsService).delete(999L);
    }

    @Test
    void getNewsByAuthor_withExistingAuthorId_shouldReturnListOfNewsListItemDto() throws Exception {
        // Given
        List<NewsModel> newsModels = Arrays.asList(newsModel);
        List<NewsListItem> newsListItems = Arrays.asList(newsListItem);
        when(newsService.findByAuthor(1L)).thenReturn(newsModels);
        when(newsApiMapper.toListItemList(newsModels)).thenReturn(newsListItems);

        // When/Then
        mockMvc.perform(get("/api/v1/news/author/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test News")))
                .andExpect(jsonPath("$[0].imageUrl", is("https://example.com/image.jpg")))
                .andExpect(jsonPath("$[0].author.id", is(1)))
                .andExpect(jsonPath("$[0].author.nickname", is("testuser")))
                .andExpect(jsonPath("$[0].commentCount", is(0)));

        verify(newsService).findByAuthor(1L);
    }

    @Test
    void getNewsByAuthor_withNonExistingAuthorId_shouldReturnNotFound() throws Exception {
        // Given
        when(newsService.findByAuthor(999L)).thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // When/Then
        mockMvc.perform(get("/api/v1/news/author/999"))
                .andExpect(status().isNotFound());

        verify(newsService).findByAuthor(999L);
    }

    @Test
    void searchNewsByTitle_shouldReturnListOfNewsListItemDto() throws Exception {
        // Given
        List<NewsModel> newsModels = Arrays.asList(newsModel);
        List<NewsListItem> newsListItems = Arrays.asList(newsListItem);
        when(newsService.findByTitle("Test")).thenReturn(newsModels);
        when(newsApiMapper.toListItemList(newsModels)).thenReturn(newsListItems);

        // When/Then
        mockMvc.perform(get("/api/v1/news/search?title=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test News")))
                .andExpect(jsonPath("$[0].imageUrl", is("https://example.com/image.jpg")))
                .andExpect(jsonPath("$[0].author.id", is(1)))
                .andExpect(jsonPath("$[0].author.nickname", is("testuser")))
                .andExpect(jsonPath("$[0].commentCount", is(0)));

        verify(newsService).findByTitle("Test");
    }

    @Test
    void searchNewsByTitle_withNoMatches_shouldReturnEmptyList() throws Exception {
        // Given
        when(newsService.findByTitle("NonExistent")).thenReturn(Arrays.asList());

        // When/Then
        mockMvc.perform(get("/api/v1/news/search?title=NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(newsService).findByTitle("NonExistent");
    }

    @Test
    void getAllNews_withPaginationParameters_shouldReturnPagedResponse() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Page<NewsModel> newsPage = new PageImpl<>(Arrays.asList(newsModel), pageable, 1);
        PagedNewsListResponse pagedResponse = new PagedNewsListResponse()
                .content(Arrays.asList(newsListItem))
                .totalElements(1L)
                .totalPages(1)
                .size(5)
                .number(0)
                .numberOfElements(1)
                .first(true)
                .last(true)
                .empty(false);
        
        when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);
        when(newsApiMapper.toPagedResponse(newsPage)).thenReturn(pagedResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/news")
                .param("page", "0")
                .param("size", "5")
                .param("sort", "creationDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(true)));

        verify(newsService).findAll(any(Pageable.class));
        verify(newsApiMapper).toPagedResponse(newsPage);
    }

    @Test
    void getAllNews_withInvalidPaginationParameters_shouldReturnBadRequest() throws Exception {
        // When/Then - Test negative page
        mockMvc.perform(get("/api/v1/news")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());

        // When/Then - Test size > 100
        mockMvc.perform(get("/api/v1/news")
                .param("page", "0")
                .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllNews_withEmptyResults_shouldReturnEmptyPagedResponse() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsModel> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        PagedNewsListResponse emptyResponse = new PagedNewsListResponse()
                .content(Arrays.asList())
                .totalElements(0L)
                .totalPages(0)
                .size(10)
                .number(0)
                .numberOfElements(0)
                .first(true)
                .last(true)
                .empty(true);
        
        when(newsService.findAll(any(Pageable.class))).thenReturn(emptyPage);
        when(newsApiMapper.toPagedResponse(emptyPage)).thenReturn(emptyResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.empty", is(true)));

        verify(newsService).findAll(any(Pageable.class));
        verify(newsApiMapper).toPagedResponse(emptyPage);
    }

    @Test
    void getAllNews_withPageBeyondAvailableData_shouldReturnEmptyPagedResponse() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(10, 10); // Page 10 when there's only 1 total element
        Page<NewsModel> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 1);
        PagedNewsListResponse emptyResponse = new PagedNewsListResponse()
                .content(Arrays.asList())
                .totalElements(1L)
                .totalPages(1)
                .size(10)
                .number(10)
                .numberOfElements(0)
                .first(false)
                .last(true)
                .empty(true);
        
        when(newsService.findAll(any(Pageable.class))).thenReturn(emptyPage);
        when(newsApiMapper.toPagedResponse(emptyPage)).thenReturn(emptyResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/news")
                .param("page", "10")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.number", is(10)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(true)))
                .andExpect(jsonPath("$.empty", is(true)));

        verify(newsService).findAll(any(Pageable.class));
        verify(newsApiMapper).toPagedResponse(emptyPage);
    }
}