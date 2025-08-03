package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.NewsRequestDto;
import com.dev.news.newsportal.dto.response.NewsListItemDto;
import com.dev.news.newsportal.dto.response.NewsResponseDto;
import com.dev.news.newsportal.dto.response.UserSummaryDto;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.NewsMapper;
import com.dev.news.newsportal.repository.NewsRepository;
import com.dev.news.newsportal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsMapper newsMapper;

    @InjectMocks
    private NewsServiceImpl newsService;

    private User author;
    private News news;
    private NewsRequestDto newsRequestDto;
    private NewsResponseDto newsResponseDto;
    private NewsListItemDto newsListItemDto;
    private UserSummaryDto authorSummaryDto;

    @BeforeEach
    void setUp() {
        // Set up test data
        author = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        news = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(LocalDateTime.now())
                .author(author)
                .comments(new ArrayList<>())
                .build();

        authorSummaryDto = UserSummaryDto.builder()
                .id(1L)
                .nickname("testuser")
                .build();

        newsRequestDto = NewsRequestDto.builder()
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .authorId(1L)
                .build();

        newsResponseDto = NewsResponseDto.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(news.getCreationDate())
                .author(authorSummaryDto)
                .commentCount(0)
                .build();

        newsListItemDto = NewsListItemDto.builder()
                .id(1L)
                .title("Test News")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(news.getCreationDate())
                .author(authorSummaryDto)
                .commentCount(0)
                .build();
    }

    @Test
    void findById_withExistingId_shouldReturnNewsResponseDto() {
        // Given
        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsMapper.toResponseDto(news)).thenReturn(newsResponseDto);

        // When
        NewsResponseDto result = newsService.findById(1L);

        // Then
        assertThat(result).isEqualTo(newsResponseDto);
        verify(newsRepository).findById(1L);
        verify(newsMapper).toResponseDto(news);
    }

    @Test
    void findById_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(newsMapper, never()).toResponseDto(any());
    }

    @Test
    void findAll_shouldReturnListOfNewsListItemDto() {
        // Given
        News news2 = News.builder()
                .id(2L)
                .title("Another News")
                .author(author)
                .build();

        NewsListItemDto newsListItemDto2 = NewsListItemDto.builder()
                .id(2L)
                .title("Another News")
                .author(authorSummaryDto)
                .build();

        List<News> newsList = Arrays.asList(news, news2);
        List<NewsListItemDto> newsListItemDtos = Arrays.asList(newsListItemDto, newsListItemDto2);
        
        when(newsRepository.findAll()).thenReturn(newsList);
        when(newsMapper.toListItemDtoList(newsList)).thenReturn(newsListItemDtos);

        // When
        List<NewsListItemDto> result = newsService.findAll();

        // Then
        assertThat(result).isEqualTo(newsListItemDtos);
        verify(newsRepository).findAll();
        verify(newsMapper).toListItemDtoList(newsList);
    }

    @Test
    void create_withValidData_shouldReturnCreatedNewsResponseDto() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(newsMapper.toEntity(newsRequestDto)).thenReturn(news);
        when(newsRepository.save(news)).thenReturn(news);
        when(newsMapper.toResponseDto(news)).thenReturn(newsResponseDto);

        // When
        NewsResponseDto result = newsService.create(newsRequestDto);

        // Then
        assertThat(result).isEqualTo(newsResponseDto);
        verify(userRepository).findById(1L);
        verify(newsMapper).toEntity(newsRequestDto);
        verify(newsRepository).save(news);
        verify(newsMapper).toResponseDto(news);
    }

    @Test
    void create_withNonExistingAuthor_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        NewsRequestDto invalidDto = NewsRequestDto.builder()
                .title("Test News")
                .text("This is a test news article")
                .authorId(999L)
                .build();

        // When/Then
        assertThatThrownBy(() -> newsService.create(invalidDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(newsMapper, never()).toEntity(any());
        verify(newsRepository, never()).save(any());
    }

    @Test
    void update_withExistingIdAndSameAuthor_shouldReturnUpdatedNewsResponseDto() {
        // Given
        News existingNews = News.builder()
                .id(1L)
                .title("Old Title")
                .text("Old text")
                .imageUrl("https://example.com/old.jpg")
                .author(author)
                .build();

        NewsRequestDto updateDto = NewsRequestDto.builder()
                .title("Updated Title")
                .text("Updated text")
                .imageUrl("https://example.com/updated.jpg")
                .authorId(1L) // Same author ID
                .build();

        News updatedNews = News.builder()
                .id(1L)
                .title("Updated Title")
                .text("Updated text")
                .imageUrl("https://example.com/updated.jpg")
                .author(author)
                .build();

        NewsResponseDto updatedResponseDto = NewsResponseDto.builder()
                .id(1L)
                .title("Updated Title")
                .text("Updated text")
                .imageUrl("https://example.com/updated.jpg")
                .author(authorSummaryDto)
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
        doNothing().when(newsMapper).updateEntityFromDto(updateDto, existingNews);
        when(newsRepository.save(existingNews)).thenReturn(updatedNews);
        when(newsMapper.toResponseDto(updatedNews)).thenReturn(updatedResponseDto);

        // When
        NewsResponseDto result = newsService.update(1L, updateDto);

        // Then
        assertThat(result).isEqualTo(updatedResponseDto);
        verify(newsRepository).findById(1L);
        verify(newsMapper).updateEntityFromDto(updateDto, existingNews);
        verify(userRepository, never()).findById(anyLong()); // Author ID is the same, so no need to find the author
        verify(newsRepository).save(existingNews);
        verify(newsMapper).toResponseDto(updatedNews);
    }

    @Test
    void update_withExistingIdAndDifferentAuthor_shouldReturnUpdatedNewsResponseDto() {
        // Given
        User newAuthor = User.builder()
                .id(2L)
                .nickname("newauthor")
                .build();

        UserSummaryDto newAuthorSummaryDto = UserSummaryDto.builder()
                .id(2L)
                .nickname("newauthor")
                .build();

        News existingNews = News.builder()
                .id(1L)
                .title("Old Title")
                .text("Old text")
                .author(author)
                .build();

        NewsRequestDto updateDto = NewsRequestDto.builder()
                .title("Updated Title")
                .text("Updated text")
                .authorId(2L) // Different author ID
                .build();

        News updatedNews = News.builder()
                .id(1L)
                .title("Updated Title")
                .text("Updated text")
                .author(newAuthor)
                .build();

        NewsResponseDto updatedResponseDto = NewsResponseDto.builder()
                .id(1L)
                .title("Updated Title")
                .text("Updated text")
                .author(newAuthorSummaryDto)
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
        doNothing().when(newsMapper).updateEntityFromDto(updateDto, existingNews);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newAuthor));
        when(newsRepository.save(existingNews)).thenReturn(updatedNews);
        when(newsMapper.toResponseDto(updatedNews)).thenReturn(updatedResponseDto);

        // When
        NewsResponseDto result = newsService.update(1L, updateDto);

        // Then
        assertThat(result).isEqualTo(updatedResponseDto);
        verify(newsRepository).findById(1L);
        verify(newsMapper).updateEntityFromDto(updateDto, existingNews);
        verify(userRepository).findById(2L); // Different author ID, so need to find the new author
        verify(newsRepository).save(existingNews);
        verify(newsMapper).toResponseDto(updatedNews);
    }

    @Test
    void update_withNonExistingId_shouldThrowResourceNotFoundException() {
        // Given
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.update(999L, newsRequestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("News not found with id: 999");

        verify(newsRepository).findById(999L);
        verify(newsMapper, never()).updateEntityFromDto(any(), any());
        verify(userRepository, never()).findById(anyLong());
        verify(newsRepository, never()).save(any());
    }

    @Test
    void update_withNonExistingAuthor_shouldThrowResourceNotFoundException() {
        // Given
        News existingNews = News.builder()
                .id(1L)
                .title("Old Title")
                .text("Old text")
                .author(author)
                .build();

        NewsRequestDto updateDto = NewsRequestDto.builder()
                .title("Updated Title")
                .text("Updated text")
                .authorId(999L) // Non-existing author ID
                .build();

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
        doNothing().when(newsMapper).updateEntityFromDto(updateDto, existingNews);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.update(1L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(newsRepository).findById(1L);
        verify(newsMapper).updateEntityFromDto(updateDto, existingNews);
        verify(userRepository).findById(999L);
        verify(newsRepository, never()).save(any());
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
                .hasMessageContaining("News not found with id: 999");

        verify(newsRepository).existsById(999L);
        verify(newsRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByAuthor_withExistingAuthorId_shouldReturnListOfNewsListItemDto() {
        // Given
        List<News> authorNews = Arrays.asList(news);
        List<NewsListItemDto> authorNewsListItemDtos = Arrays.asList(newsListItemDto);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(newsRepository.findByAuthor(author)).thenReturn(authorNews);
        when(newsMapper.toListItemDtoList(authorNews)).thenReturn(authorNewsListItemDtos);

        // When
        List<NewsListItemDto> result = newsService.findByAuthor(1L);

        // Then
        assertThat(result).isEqualTo(authorNewsListItemDtos);
        verify(userRepository).findById(1L);
        verify(newsRepository).findByAuthor(author);
        verify(newsMapper).toListItemDtoList(authorNews);
    }

    @Test
    void findByAuthor_withNonExistingAuthorId_shouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> newsService.findByAuthor(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(newsRepository, never()).findByAuthor(any());
        verify(newsMapper, never()).toListItemDtoList(any());
    }

    @Test
    void findByTitle_shouldReturnListOfNewsListItemDto() {
        // Given
        List<News> matchingNews = Arrays.asList(news);
        List<NewsListItemDto> matchingNewsListItemDtos = Arrays.asList(newsListItemDto);
        
        when(newsRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(matchingNews);
        when(newsMapper.toListItemDtoList(matchingNews)).thenReturn(matchingNewsListItemDtos);

        // When
        List<NewsListItemDto> result = newsService.findByTitle("Test");

        // Then
        assertThat(result).isEqualTo(matchingNewsListItemDtos);
        verify(newsRepository).findByTitleContainingIgnoreCase("Test");
        verify(newsMapper).toListItemDtoList(matchingNews);
    }
}