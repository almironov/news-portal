package com.dev.news.newsportal.mapper.api;

import com.dev.news.newsportal.api.model.news.NewsListItem;
import com.dev.news.newsportal.api.model.news.NewsRequest;
import com.dev.news.newsportal.api.model.news.NewsResponse;
import com.dev.news.newsportal.api.model.news.PagedNewsListResponse;
import com.dev.news.newsportal.model.CommentModel;
import com.dev.news.newsportal.model.NewsModel;
import com.dev.news.newsportal.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class NewsApiMapperTest {

    @Autowired
    private NewsApiMapper newsApiMapper;
    
    private NewsModel newsModel;
    private UserModel userModel;
    private CommentModel commentModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();

        userModel = UserModel.builder()
                .id(1L)
                .nickname("testuser")
                .email("test@example.com")
                .role("USER")
                .build();

        commentModel = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .build();

        newsModel = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(userModel)
                .comments(Arrays.asList(commentModel))
                .build();
    }

    @Test
    void toModel_shouldConvertNewsRequestToNewsModel() {
        // Given
        NewsRequest newsRequest = new NewsRequest("Test News", "This is a test news article", 1L)
                .imageUrl(URI.create("https://example.com/image.jpg"));

        // When
        NewsModel result = newsApiMapper.toModel(newsRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // Should be ignored
        assertThat(result.getTitle()).isEqualTo("Test News");
        assertThat(result.getText()).isEqualTo("This is a test news article");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(result.getCreationDate()).isNull(); // Should be ignored
        assertThat(result.getAuthor()).isNull(); // Should be ignored
        assertThat(result.getComments()).isEmpty(); // Should be ignored and default to empty list
    }

    @Test
    void toModel_withNullImageUrl_shouldHandleGracefully() {
        // Given
        NewsRequest newsRequest = new NewsRequest("Test News", "This is a test news article", 1L);

        // When
        NewsModel result = newsApiMapper.toModel(newsRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isNull();
    }

    @Test
    void toResponse_shouldConvertNewsModelToNewsResponse() {
        // Given
        OffsetDateTime expectedDateTime = creationDate.atOffset(ZoneOffset.UTC);

        // When
        NewsResponse result = newsApiMapper.toResponse(newsModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test News");
        assertThat(result.getText()).isEqualTo("This is a test news article");
        assertThat(result.getImageUrl()).isEqualTo(URI.create("https://example.com/image.jpg"));
        assertThat(result.getCreationDate()).isEqualTo(expectedDateTime);
        assertThat(result.getCommentCount()).isEqualTo(1L);
        
        // Check author mapping
        assertThat(result.getAuthor()).isNotNull();
        assertThat(result.getAuthor().getId()).isEqualTo(1L);
        assertThat(result.getAuthor().getNickname()).isEqualTo("testuser");
    }

    @Test
    void toResponse_withNullComments_shouldReturnZeroCommentCount() {
        // Given
        NewsModel newsModelWithoutComments = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl("https://example.com/image.jpg")
                .creationDate(creationDate)
                .author(userModel)
                .comments(null)
                .build();

        // When
        NewsResponse result = newsApiMapper.toResponse(newsModelWithoutComments);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCommentCount()).isEqualTo(0L);
    }

    @Test
    void toResponse_withNullImageUrl_shouldHandleGracefully() {
        // Given
        NewsModel newsModelWithoutImage = NewsModel.builder()
                .id(1L)
                .title("Test News")
                .text("This is a test news article")
                .imageUrl(null)
                .creationDate(creationDate)
                .author(userModel)
                .comments(Arrays.asList(commentModel))
                .build();

        // When
        NewsResponse result = newsApiMapper.toResponse(newsModelWithoutImage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getImageUrl()).isNull();
    }

    @Test
    void toListItem_shouldConvertNewsModelToNewsListItem() {
        // Given
        OffsetDateTime expectedDateTime = creationDate.atOffset(ZoneOffset.UTC);

        // When
        NewsListItem result = newsApiMapper.toListItem(newsModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test News");
        assertThat(result.getImageUrl()).isEqualTo(URI.create("https://example.com/image.jpg"));
        assertThat(result.getCreationDate()).isEqualTo(expectedDateTime);
        assertThat(result.getCommentCount()).isEqualTo(1L);
        
        // Check author mapping
        assertThat(result.getAuthor()).isNotNull();
        assertThat(result.getAuthor().getId()).isEqualTo(1L);
        assertThat(result.getAuthor().getNickname()).isEqualTo("testuser");
    }

    @Test
    void toResponseList_shouldConvertListOfNewsModelsToNewsResponses() {
        // Given
        List<NewsModel> newsModels = Arrays.asList(newsModel);

        // When
        List<NewsResponse> result = newsApiMapper.toResponseList(newsModels);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test News");
    }

    @Test
    void toListItemList_shouldConvertListOfNewsModelsToNewsListItems() {
        // Given
        List<NewsModel> newsModels = Arrays.asList(newsModel);

        // When
        List<NewsListItem> result = newsApiMapper.toListItemList(newsModels);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test News");
    }

    @Test
    void uriToString_shouldConvertUriToString() {
        // Given
        URI uri = URI.create("https://example.com/image.jpg");

        // When
        String result = newsApiMapper.uriToString(uri);

        // Then
        assertThat(result).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    void uriToString_withNullUri_shouldReturnNull() {
        // When
        String result = newsApiMapper.uriToString(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToUri_shouldConvertStringToUri() {
        // Given
        String uriString = "https://example.com/image.jpg";

        // When
        URI result = newsApiMapper.stringToUri(uriString);

        // Then
        assertThat(result).isEqualTo(URI.create("https://example.com/image.jpg"));
    }

    @Test
    void stringToUri_withNullString_shouldReturnNull() {
        // When
        URI result = newsApiMapper.stringToUri(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToUri_withEmptyString_shouldReturnNull() {
        // When
        URI result = newsApiMapper.stringToUri("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToUri_withWhitespaceString_shouldReturnNull() {
        // When
        URI result = newsApiMapper.stringToUri("   ");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToUri_withInvalidUri_shouldReturnNull() {
        // Given
        String invalidUri = "not a valid uri";

        // When
        URI result = newsApiMapper.stringToUri(invalidUri);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toOffsetDateTime_shouldConvertLocalDateTimeToOffsetDateTime() {
        // When
        OffsetDateTime result = newsApiMapper.toOffsetDateTime(creationDate);

        // Then
        assertThat(result).isEqualTo(creationDate.atOffset(ZoneOffset.UTC));
    }

    @Test
    void toOffsetDateTime_withNullLocalDateTime_shouldReturnNull() {
        // When
        OffsetDateTime result = newsApiMapper.toOffsetDateTime(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toLocalDateTime_shouldConvertOffsetDateTimeToLocalDateTime() {
        // Given
        OffsetDateTime offsetDateTime = creationDate.atOffset(ZoneOffset.UTC);

        // When
        LocalDateTime result = newsApiMapper.toLocalDateTime(offsetDateTime);

        // Then
        assertThat(result).isEqualTo(creationDate);
    }

    @Test
    void toLocalDateTime_withNullOffsetDateTime_shouldReturnNull() {
        // When
        LocalDateTime result = newsApiMapper.toLocalDateTime(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toPagedResponse_shouldConvertPageToPagedNewsListResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsModel> newsPage = new PageImpl<>(Arrays.asList(newsModel), pageable, 1);

        // When
        PagedNewsListResponse result = newsApiMapper.toPagedResponse(newsPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test News");
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getFirst()).isTrue();
        assertThat(result.getLast()).isTrue();
        assertThat(result.getEmpty()).isFalse();
        assertThat(result.getPageable()).isNotNull();
    }

    @Test
    void toPagedResponse_withEmptyPage_shouldReturnEmptyPagedResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsModel> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

        // When
        PagedNewsListResponse result = newsApiMapper.toPagedResponse(emptyPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getFirst()).isTrue();
        assertThat(result.getLast()).isTrue();
        assertThat(result.getEmpty()).isTrue();
        assertThat(result.getPageable()).isNotNull();
    }

    @Test
    void toPagedResponse_withMultiplePages_shouldMapPaginationMetadataCorrectly() {
        // Given
        Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page
        List<NewsModel> newsModels = Arrays.asList(newsModel);
        Page<NewsModel> newsPage = new PageImpl<>(newsModels, pageable, 15); // Total 15 items

        // When
        PagedNewsListResponse result = newsApiMapper.toPagedResponse(newsPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(15L);
        assertThat(result.getTotalPages()).isEqualTo(3); // 15 items / 5 per page = 3 pages
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getNumber()).isEqualTo(1); // Second page (0-based)
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getFirst()).isFalse(); // Not first page
        assertThat(result.getLast()).isFalse(); // Not last page (page 1 of 3)
        assertThat(result.getEmpty()).isFalse();
        assertThat(result.getPageable()).isNotNull();
        assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(5);
    }

    @Test
    void toPagedResponse_shouldMapContentCorrectly() {
        // Given
        UserModel secondUser = UserModel.builder()
                .id(2L)
                .nickname("seconduser")
                .email("second@example.com")
                .role("USER")
                .build();

        NewsModel secondNews = NewsModel.builder()
                .id(2L)
                .title("Second News")
                .text("This is the second news article")
                .imageUrl("https://example.com/image2.jpg")
                .creationDate(creationDate.plusHours(1))
                .author(secondUser)
                .comments(Arrays.asList())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<NewsModel> newsPage = new PageImpl<>(Arrays.asList(newsModel, secondNews), pageable, 2);

        // When
        PagedNewsListResponse result = newsApiMapper.toPagedResponse(newsPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        
        // Verify first news item
        NewsListItem firstItem = result.getContent().get(0);
        assertThat(firstItem.getId()).isEqualTo(1L);
        assertThat(firstItem.getTitle()).isEqualTo("Test News");
        assertThat(firstItem.getAuthor().getId()).isEqualTo(1L);
        assertThat(firstItem.getAuthor().getNickname()).isEqualTo("testuser");
        assertThat(firstItem.getCommentCount()).isEqualTo(1L);
        
        // Verify second news item
        NewsListItem secondItem = result.getContent().get(1);
        assertThat(secondItem.getId()).isEqualTo(2L);
        assertThat(secondItem.getTitle()).isEqualTo("Second News");
        assertThat(secondItem.getAuthor().getId()).isEqualTo(2L);
        assertThat(secondItem.getAuthor().getNickname()).isEqualTo("seconduser");
        assertThat(secondItem.getCommentCount()).isEqualTo(0L);
    }
}