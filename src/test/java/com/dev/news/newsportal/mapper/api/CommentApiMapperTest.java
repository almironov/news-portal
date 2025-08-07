package com.dev.news.newsportal.mapper.api;

import com.dev.news.newsportal.api.model.comments.CommentListItem;
import com.dev.news.newsportal.api.model.comments.CommentRequest;
import com.dev.news.newsportal.api.model.comments.CommentResponse;
import com.dev.news.newsportal.model.CommentModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CommentApiMapperTest {

    @Autowired
    private CommentApiMapper commentApiMapper;
    
    private CommentModel commentModel;
    private CommentModel replyModel;
    private LocalDateTime creationDate;

    @BeforeEach
    void setUp() {
        creationDate = LocalDateTime.now();

        replyModel = CommentModel.builder()
                .id(2L)
                .text("Reply comment")
                .authorNickname("replyuser")
                .creationDate(creationDate.minusHours(1))
                .newsId(1L)
                .parentCommentId(1L)
                .replies(new ArrayList<>())
                .build();

        commentModel = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .parentCommentId(null)
                .replies(Arrays.asList(replyModel))
                .build();
    }

    @Test
    void toModel_shouldConvertCommentRequestToCommentModel() {
        // Given
        CommentRequest commentRequest = new CommentRequest("Test comment", "testuser", 1L)
                .parentCommentId(null);

        // When
        CommentModel result = commentApiMapper.toModel(commentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // Should be ignored
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("testuser");
        assertThat(result.getNewsId()).isEqualTo(1L);
        assertThat(result.getParentCommentId()).isNull();
        assertThat(result.getCreationDate()).isNull(); // Should be ignored
        assertThat(result.getReplies()).isEmpty(); // Should be ignored and default to empty list
    }

    @Test
    void toModel_withParentCommentId_shouldConvertCorrectly() {
        // Given
        CommentRequest commentRequest = new CommentRequest("Reply comment", "replyuser", 1L)
                .parentCommentId(5L);

        // When
        CommentModel result = commentApiMapper.toModel(commentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Reply comment");
        assertThat(result.getAuthorNickname()).isEqualTo("replyuser");
        assertThat(result.getNewsId()).isEqualTo(1L);
        assertThat(result.getParentCommentId()).isEqualTo(5L);
    }

    @Test
    void toResponse_shouldConvertCommentModelToCommentResponse() {
        // Given
        OffsetDateTime expectedDateTime = creationDate.atOffset(ZoneOffset.UTC);

        // When
        CommentResponse result = commentApiMapper.toResponse(commentModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("testuser");
        assertThat(result.getCreationDate()).isEqualTo(expectedDateTime);
        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().get(0).getId()).isEqualTo(2L);
    }

    @Test
    void toResponse_withNullCreationDate_shouldHandleGracefully() {
        // Given
        CommentModel commentWithoutDate = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(null)
                .newsId(1L)
                .replies(new ArrayList<>())
                .build();

        // When
        CommentResponse result = commentApiMapper.toResponse(commentWithoutDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreationDate()).isNull();
    }

    @Test
    void toResponse_withNullCommentModel_shouldReturnNull() {
        // When
        CommentResponse result = commentApiMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toListItem_shouldConvertCommentModelToCommentListItem() {
        // Given
        OffsetDateTime expectedDateTime = creationDate.atOffset(ZoneOffset.UTC);

        // When
        CommentListItem result = commentApiMapper.toListItem(commentModel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test comment");
        assertThat(result.getAuthorNickname()).isEqualTo("testuser");
        assertThat(result.getCreationDate()).isEqualTo(expectedDateTime);
        assertThat(result.getHasReplies()).isTrue(); // Has replies
    }

    @Test
    void toListItem_withNoReplies_shouldSetHasRepliesToFalse() {
        // Given
        CommentModel commentWithoutReplies = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .replies(new ArrayList<>())
                .build();

        // When
        CommentListItem result = commentApiMapper.toListItem(commentWithoutReplies);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHasReplies()).isFalse(); // No replies
    }

    @Test
    void toListItem_withNullReplies_shouldSetHasRepliesToFalse() {
        // Given
        CommentModel commentWithNullReplies = CommentModel.builder()
                .id(1L)
                .text("Test comment")
                .authorNickname("testuser")
                .creationDate(creationDate)
                .newsId(1L)
                .replies(null)
                .build();

        // When
        CommentListItem result = commentApiMapper.toListItem(commentWithNullReplies);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getHasReplies()).isFalse(); // Null replies
    }

    @Test
    void toListItem_withNullCommentModel_shouldReturnNull() {
        // When
        CommentListItem result = commentApiMapper.toListItem(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toResponseList_shouldConvertListOfCommentModelsToCommentResponses() {
        // Given
        List<CommentModel> commentModels = Arrays.asList(commentModel, replyModel);

        // When
        List<CommentResponse> result = commentApiMapper.toResponseList(commentModels);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
        assertThat(result.get(0).getAuthorNickname()).isEqualTo("testuser");
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getText()).isEqualTo("Reply comment");
        assertThat(result.get(1).getAuthorNickname()).isEqualTo("replyuser");
    }

    @Test
    void toResponseList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<CommentModel> emptyList = Arrays.asList();

        // When
        List<CommentResponse> result = commentApiMapper.toResponseList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toResponseList_withNullList_shouldReturnNull() {
        // When
        List<CommentResponse> result = commentApiMapper.toResponseList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toListItemList_shouldConvertListOfCommentModelsToCommentListItems() {
        // Given
        List<CommentModel> commentModels = Arrays.asList(commentModel, replyModel);

        // When
        List<CommentListItem> result = commentApiMapper.toListItemList(commentModels);

        // Then
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getText()).isEqualTo("Test comment");
        assertThat(result.get(0).getHasReplies()).isTrue();
        
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getText()).isEqualTo("Reply comment");
        assertThat(result.get(1).getHasReplies()).isFalse();
    }

    @Test
    void toListItemList_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<CommentModel> emptyList = Arrays.asList();

        // When
        List<CommentListItem> result = commentApiMapper.toListItemList(emptyList);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void toListItemList_withNullList_shouldReturnNull() {
        // When
        List<CommentListItem> result = commentApiMapper.toListItemList(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toOffsetDateTime_shouldConvertLocalDateTimeToOffsetDateTime() {
        // When
        OffsetDateTime result = commentApiMapper.toOffsetDateTime(creationDate);

        // Then
        assertThat(result).isEqualTo(creationDate.atOffset(ZoneOffset.UTC));
    }

    @Test
    void toOffsetDateTime_withNullLocalDateTime_shouldReturnNull() {
        // When
        OffsetDateTime result = commentApiMapper.toOffsetDateTime(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toLocalDateTime_shouldConvertOffsetDateTimeToLocalDateTime() {
        // Given
        OffsetDateTime offsetDateTime = creationDate.atOffset(ZoneOffset.UTC);

        // When
        LocalDateTime result = commentApiMapper.toLocalDateTime(offsetDateTime);

        // Then
        assertThat(result).isEqualTo(creationDate);
    }

    @Test
    void toLocalDateTime_withNullOffsetDateTime_shouldReturnNull() {
        // When
        LocalDateTime result = commentApiMapper.toLocalDateTime(null);

        // Then
        assertThat(result).isNull();
    }
}