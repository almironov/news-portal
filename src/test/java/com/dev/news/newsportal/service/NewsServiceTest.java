package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.NewsEntityMapper;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.repository.NewsRepository;
import com.dev.news.newsportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsEntityMapper newsEntityMapper;

    @Mock
    private UserEntityMapper userEntityMapper;

    private NewsServiceImpl newsService;

    private User authorEntity;
    private News newsEntity;
    private UserModel authorModel;
    private NewsModel newsModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        // Reset all mocks to ensure test isolation
        MockitoAnnotations.openMocks(this);
        
        // Manually instantiate service with mocked dependencies
        newsService = new NewsServiceImpl(newsRepository, userRepository, newsEntityMapper, userEntityMapper);
        
        creationDate = LocalDateTime.now();
        
        // Set up entity data
        authorEntity = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        newsEntity = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        // Set up domain model data
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
                .comments(new ArrayList<>())
                .build();
    }

    @Test
    void findById_withExistingId_shouldReturnNewsModel() {
        // Given
        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(newsEntityMapper.toModel(newsEntity)).thenReturn(newsModel);

        // When
        NewsModel result = newsService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test News");
        assertThat(result.getText()).isEqualTo("This is a test news article");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(result.getAuthor().getId()).isEqualTo(1L);
        assertThat(result.getAuthor().getNickname()).isEqualTo("testuser");

        verify(newsRepository).findById(1L);
        verify(newsEntityMapper).toModel(newsEntity);
    }

    @Test
    void findById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(newsEntityMapper, never()).toModel(any(News.class));
    }

    @Test
    void findAll_shouldReturnListOfNewsModels() {
        // Given
        List<News> newsEntities = Arrays.asList(newsEntity);
        List<NewsModel> newsModels = Arrays.asList(newsModel);
        when(newsRepository.findAll()).thenReturn(newsEntities);
        when(newsEntityMapper.toModelList(newsEntities)).thenReturn(newsModels);

        // When
        List<NewsModel> result = newsService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test News");

        verify(newsRepository).findAll();
        verify(newsEntityMapper).toModelList(newsEntities);
    }

    @Test
    void create_withValidNewsModel_shouldReturnCreatedNewsModel() {
        // Given
        NewsModel inputModel = NewsModel.builder()
                .title("New News")
                .text("New news content")
                .imageUrl("https://example.com/new-image.jpg")
                .author(authorModel)
                .build();

        News inputEntity = News.builder()
                .title("New News")
                .text("New news content")
                .imageUrl("https://example.com/new-image.jpg")
                .author(authorEntity)
                .build();

        News savedEntity = News.builder()
                .id(2L)
                .title("New News")
                .text("New news content")
                .imageUrl("https://example.com/new-image.jpg")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        NewsModel savedModel = NewsModel.builder()
                .id(2L)
                .title("New News")
                .text("New news content")
                .imageUrl("https://example.com/new-image.jpg")
                .creationDate(creationDate)
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(newsEntityMapper.toEntity(inputModel)).thenReturn(inputEntity);
        when(newsRepository.save(any(News.class))).thenReturn(savedEntity);
        when(newsEntityMapper.toModel(savedEntity)).thenReturn(savedModel);

        // When
        NewsModel result = newsService.create(inputModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New News");
        assertThat(result.getText()).isEqualTo("New news content");

        verify(userRepository).findById(1L);
        verify(newsEntityMapper).toEntity(inputModel);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).toModel(savedEntity);
    }

    @Test
    void create_withNonExistingAuthor_shouldThrowResourceNotFoundException() {
        // Given
        NewsModel inputModel = NewsModel.builder()
                .title("New News")
                .text("New news content")
                .author(UserModel.builder().id(999L).build())
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.create(inputModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void update_withExistingIdAndValidData_shouldReturnUpdatedNewsModel() {
        // Given
        NewsModel updateModel = NewsModel.builder()
                .title("Updated News")
                .text("Updated news content")
                .imageUrl("https://example.com/updated-image.jpg")
                .author(authorModel)
                .build();

        News updatedEntity = News.builder()
                .id(1L)
                .title("Updated News")
                .text("Updated news content")
                .imageUrl("https://example.com/updated-image.jpg")
                .creationDate(creationDate)
                .author(authorEntity)
                .comments(new ArrayList<>())
                .build();

        NewsModel updatedModel = NewsModel.builder()
                .id(1L)
                .title("Updated News")
                .text("Updated news content")
                .imageUrl("https://example.com/updated-image.jpg")
                .creationDate(creationDate)
                .author(authorModel)
                .comments(new ArrayList<>())
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(newsEntity));
        when(newsRepository.save(any(News.class))).thenReturn(updatedEntity);
        when(newsEntityMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        // When
        NewsModel result = newsService.update(1L, updateModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Updated News");
        assertThat(result.getText()).isEqualTo("Updated news content");

        verify(newsRepository).findById(1L);
        verify(newsRepository).save(any(News.class));
        verify(newsEntityMapper).toModel(updatedEntity);
    }

    @Test
    void update_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        NewsModel updateModel = NewsModel.builder()
                .title("Updated News")
                .text("Updated news content")
                .author(authorModel)
                .build();

        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.update(999L, updateModel))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(newsRepository, never()).save(any(News.class));
    }

    @Test
    void delete_withExistingId_shouldDeleteNews() {
        // Given
        when(newsRepository.existsById(1L)).thenReturn(true);

        // When
        newsService.delete(1L);

        // Then
        verify(newsRepository).existsById(1L);
        verify(newsRepository).deleteById(1L);
    }

    @Test
    void delete_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> newsService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("News not found with id: 999");

        verify(newsRepository).existsById(999L);
        verify(newsRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByAuthor_withExistingAuthorId_shouldReturnListOfNewsModels() {
        // Given
        List<News> newsEntities = Arrays.asList(newsEntity);
        List<NewsModel> newsModels = Arrays.asList(newsModel);
        when(userRepository.findById(1L)).thenReturn(Optional.of(authorEntity));
        when(newsRepository.findByAuthor(authorEntity)).thenReturn(newsEntities);
        when(newsEntityMapper.toModelList(newsEntities)).thenReturn(newsModels);

        // When
        List<NewsModel> result = newsService.findByAuthor(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAuthor().getId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(newsRepository).findByAuthor(authorEntity);
        verify(newsEntityMapper).toModelList(newsEntities);
    }

    @Test
    void findByAuthor_withNonExistingAuthorId_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.findByAuthor(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(newsRepository, never()).findByAuthor(any(User.class));
    }

    @Test
    void findByTitle_shouldReturnListOfNewsModels() {
        // Given
        List<News> newsEntities = Arrays.asList(newsEntity);
        List<NewsModel> newsModels = Arrays.asList(newsModel);
        when(newsRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(newsEntities);
        when(newsEntityMapper.toModelList(newsEntities)).thenReturn(newsModels);

        // When
        List<NewsModel> result = newsService.findByTitle("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Test");

        verify(newsRepository).findByTitleContainingIgnoreCase("Test");
        verify(newsEntityMapper).toModelList(newsEntities);
    }

    @Test
    void findByTitle_withNoMatches_shouldReturnEmptyList() {
        // Given
        when(newsRepository.findByTitleContainingIgnoreCase("NonExistent")).thenReturn(Arrays.asList());
        when(newsEntityMapper.toModelList(Arrays.asList())).thenReturn(Arrays.asList());

        // When
        List<NewsModel> result = newsService.findByTitle("NonExistent");

        // Then
        assertThat(result).isEmpty();

        verify(newsRepository).findByTitleContainingIgnoreCase("NonExistent");
        verify(newsEntityMapper).toModelList(Arrays.asList());
    }

    @Test
    void findAll_WithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("creationDate").descending());
        Page<News> newsPage = new PageImpl<>(Arrays.asList(newsEntity), pageable, 1);
        when(newsRepository.findAll(pageable)).thenReturn(newsPage);
        when(newsEntityMapper.toModel(newsEntity)).thenReturn(newsModel);

        // When
        Page<NewsModel> result = newsService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(newsRepository).findAll(pageable);
    }

    @Test
    void findAll_WithEmptyPage_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 10);
        Page<News> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(newsRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<NewsModel> result = newsService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        verify(newsRepository).findAll(pageable);
    }

    @Test
    void findAll_WithDifferentSortOrders_ShouldReturnSortedResults() {
        // Given
        Pageable pageableAsc = PageRequest.of(0, 5, Sort.by("title").ascending());
        Page<News> newsPageAsc = new PageImpl<>(Arrays.asList(newsEntity), pageableAsc, 1);
        when(newsRepository.findAll(pageableAsc)).thenReturn(newsPageAsc);
        when(newsEntityMapper.toModel(newsEntity)).thenReturn(newsModel);

        // When
        Page<NewsModel> result = newsService.findAll(pageableAsc);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSort().getOrderFor("title")).isNotNull();
        assertThat(result.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
        verify(newsRepository).findAll(pageableAsc);
    }

    @Test
    void findAll_WithMultipleSortCriteria_ShouldReturnSortedResults() {
        // Given
        Sort multiSort = Sort.by(Sort.Order.desc("creationDate"), Sort.Order.asc("title"));
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Page<News> newsPage = new PageImpl<>(Arrays.asList(newsEntity), pageable, 1);
        when(newsRepository.findAll(pageable)).thenReturn(newsPage);
        when(newsEntityMapper.toModel(newsEntity)).thenReturn(newsModel);

        // When
        Page<NewsModel> result = newsService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getSort().isSorted()).isTrue();
        assertThat(result.getSort().getOrderFor("creationDate").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(result.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
        verify(newsRepository).findAll(pageable);
    }
}