package com.dev.news.newsportal.mapper.api;

import com.dev.news.newsportal.api.model.comments.CommentListItem;
import com.dev.news.newsportal.api.model.comments.CommentRequest;
import com.dev.news.newsportal.api.model.comments.CommentResponse;
import com.dev.news.newsportal.model.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentApiMapper {

    // CommentRequest to CommentModel
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "replies", ignore = true)
    CommentModel toModel(CommentRequest commentRequest);

    // CommentModel to CommentResponse
    @Mapping(target = "creationDate", expression = "java(toOffsetDateTime(commentModel.getCreationDate()))")
    @Mapping(target = "replies", source = "replies")
    CommentResponse toResponse(CommentModel commentModel);

    // CommentModel to CommentListItem
    @Mapping(target = "creationDate", expression = "java(toOffsetDateTime(commentModel.getCreationDate()))")
    @Mapping(target = "hasReplies", expression = "java(commentModel.getReplies() != null && !commentModel.getReplies().isEmpty())")
    CommentListItem toListItem(CommentModel commentModel);

    // List mappings
    List<CommentResponse> toResponseList(List<CommentModel> commentModels);

    List<CommentListItem> toListItemList(List<CommentModel> commentModels);

    // Helper method for date conversion
    default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }

    default LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}