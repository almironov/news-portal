package com.dev.news.newsportal.mapper;

import com.dev.news.newsportal.dto.request.NewsRequestDto;
import com.dev.news.newsportal.dto.response.NewsListItemDto;
import com.dev.news.newsportal.dto.response.NewsResponseDto;
import com.dev.news.newsportal.dto.response.UserSummaryDto;
import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NewsMapperTest {

    @Autowired
    private NewsMapper newsMapper;

    @Test
    void toEntity_shouldMapDtoToEntity() {
        // Given
        NewsRequestDto dto = NewsRequestDto.builder()
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .authorId(1L)
                .build();

        // When
        News news = newsMapper.toEntity(dto);

        // Then
        assertThat(news).isNotNull();
        assertThat(news.getId()).isNull(); // ID should be ignored in mapping
        assertThat(news.getTitle()).isEqualTo(dto.getTitle());
        assertThat(news.getText()).isEqualTo(dto.getText());
        assertThat(news.getImageUrl()).isEqualTo(dto.getImageUrl());
        assertThat(news.getCreationDate()).isNull(); // CreationDate should be ignored in mapping
        assertThat(news.getComments()).isNotNull(); // Comments should be initialized as empty list
        assertThat(news.getComments()).isEmpty();
        assertThat(news.getAuthor()).isNotNull();
        assertThat(news.getAuthor().getId()).isEqualTo(dto.getAuthorId());
    }

    @Test
    void toResponseDto_shouldMapEntityToDto() {
        // Given
        User author = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder().id(1L).text("Comment 1").build());
        comments.add(Comment.builder().id(2L).text("Comment 2").build());

        News news = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(LocalDateTime.now())
                .author(author)
                .comments(comments)
                .build();

        // When
        NewsResponseDto dto = newsMapper.toResponseDto(news);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(news.getId());
        assertThat(dto.getTitle()).isEqualTo(news.getTitle());
        assertThat(dto.getText()).isEqualTo(news.getText());
        assertThat(dto.getImageUrl()).isEqualTo(news.getImageUrl());
        assertThat(dto.getCreationDate()).isEqualTo(news.getCreationDate());
        assertThat(dto.getCommentCount()).isEqualTo(news.getComments().size());
        
        // Check author mapping
        assertThat(dto.getAuthor()).isNotNull();
        assertThat(dto.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(dto.getAuthor().getNickname()).isEqualTo(author.getNickname());
    }

    @Test
    void toListItemDto_shouldMapEntityToListItemDto() {
        // Given
        User author = User.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        List<Comment> comments = new ArrayList<>();
        comments.add(Comment.builder().id(1L).text("Comment 1").build());
        comments.add(Comment.builder().id(2L).text("Comment 2").build());

        News news = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(LocalDateTime.now())
                .author(author)
                .comments(comments)
                .build();

        // When
        NewsListItemDto dto = newsMapper.toListItemDto(news);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(news.getId());
        assertThat(dto.getTitle()).isEqualTo(news.getTitle());
        assertThat(dto.getImageUrl()).isEqualTo(news.getImageUrl());
        assertThat(dto.getCreationDate()).isEqualTo(news.getCreationDate());
        assertThat(dto.getCommentCount()).isEqualTo(news.getComments().size());
        
        // Check author mapping
        assertThat(dto.getAuthor()).isNotNull();
        assertThat(dto.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(dto.getAuthor().getNickname()).isEqualTo(author.getNickname());
        
        // Text field should not be included in list item DTO
        // We can't directly test this as the field exists in the DTO, but we can verify it's mapped correctly
    }

    @Test
    void toListItemDtoList_shouldMapEntityListToDtoList() {
        // Given
        User author1 = User.builder()
                .id(1L)
                .nickname("user1")
                .build();

        User author2 = User.builder()
                .id(2L)
                .nickname("user2")
                .build();

        News news1 = News.builder()
                .id(1L)
                .title("News 1")
                .author(author1)
                .comments(new ArrayList<>())
                .build();

        News news2 = News.builder()
                .id(2L)
                .title("News 2")
                .author(author2)
                .comments(Arrays.asList(Comment.builder().id(1L).build()))
                .build();

        List<News> newsList = Arrays.asList(news1, news2);

        // When
        List<NewsListItemDto> dtoList = newsMapper.toListItemDtoList(newsList);

        // Then
        assertThat(dtoList).isNotNull();
        assertThat(dtoList).hasSize(2);
        
        // Check first news item
        assertThat(dtoList.get(0).getId()).isEqualTo(news1.getId());
        assertThat(dtoList.get(0).getTitle()).isEqualTo(news1.getTitle());
        assertThat(dtoList.get(0).getAuthor().getId()).isEqualTo(author1.getId());
        assertThat(dtoList.get(0).getAuthor().getNickname()).isEqualTo(author1.getNickname());
        assertThat(dtoList.get(0).getCommentCount()).isEqualTo(0);
        
        // Check second news item
        assertThat(dtoList.get(1).getId()).isEqualTo(news2.getId());
        assertThat(dtoList.get(1).getTitle()).isEqualTo(news2.getTitle());
        assertThat(dtoList.get(1).getAuthor().getId()).isEqualTo(author2.getId());
        assertThat(dtoList.get(1).getAuthor().getNickname()).isEqualTo(author2.getNickname());
        assertThat(dtoList.get(1).getCommentCount()).isEqualTo(1);
    }

    @Test
    void updateEntityFromDto_shouldUpdateEntityWithDtoValues() {
        // Given
        User author = User.builder()
                .id(1L)
                .nickname("testuser")
                .build();

        News existingNews = News.builder()
                .id(1L)
                .title("Old Title")
                .text("Old text")
                .imageUrl("https://example.com/old.jpg")
                .creationDate(LocalDateTime.now())
                .author(author)
                .comments(new ArrayList<>())
                .build();

        NewsRequestDto dto = NewsRequestDto.builder()
                .title("New Title")
                .text("New text")
                .imageUrl("https://example.com/new.jpg")
                .authorId(2L) // Different author ID, but author should not be updated
                .build();

        // When
        newsMapper.updateEntityFromDto(dto, existingNews);

        // Then
        assertThat(existingNews.getId()).isEqualTo(1L); // ID should not be changed
        assertThat(existingNews.getTitle()).isEqualTo(dto.getTitle());
        assertThat(existingNews.getText()).isEqualTo(dto.getText());
        assertThat(existingNews.getImageUrl()).isEqualTo(dto.getImageUrl());
        assertThat(existingNews.getCreationDate()).isNotNull(); // CreationDate should not be changed
        assertThat(existingNews.getAuthor()).isEqualTo(author); // Author should not be changed
        assertThat(existingNews.getComments()).isNotNull(); // Comments should not be changed
        assertThat(existingNews.getComments()).isEmpty();
    }

    @Test
    void toResponseDto_withNullComments_shouldHandleGracefully() {
        // Given
        User author = User.builder()
                .id(1L)
                .nickname("testuser")
                .build();

        News news = News.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .author(author)
                .comments(null) // Explicitly set comments to null
                .build();

        // When
        NewsResponseDto dto = newsMapper.toResponseDto(news);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getCommentCount()).isEqualTo(0); // Should handle null comments gracefully
    }
}