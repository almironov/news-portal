package com.dev.news.newsportal.mapper.api;

import com.dev.news.newsportal.api.model.news.NewsListItem;
import com.dev.news.newsportal.api.model.news.NewsRequest;
import com.dev.news.newsportal.api.model.news.NewsResponse;
import com.dev.news.newsportal.api.model.news.PagedNewsListResponse;
import com.dev.news.newsportal.model.NewsModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserApiMapper.class})
public interface NewsApiMapper {

    // NewsRequest to NewsModel
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "imageUrl", expression = "java(uriToString(newsRequest.getImageUrl()))")
    NewsModel toModel(NewsRequest newsRequest);

    // NewsModel to NewsResponse
    @Mapping(target = "creationDate", expression = "java(toOffsetDateTime(newsModel.getCreationDate()))")
    @Mapping(target = "imageUrl", expression = "java(stringToUri(newsModel.getImageUrl()))")
    @Mapping(target = "commentCount", expression = "java(newsModel.getComments() != null ? (long) newsModel.getComments().size() : 0L)")
    NewsResponse toResponse(NewsModel newsModel);

    // NewsModel to NewsListItem
    @Mapping(target = "creationDate", expression = "java(toOffsetDateTime(newsModel.getCreationDate()))")
    @Mapping(target = "imageUrl", expression = "java(stringToUri(newsModel.getImageUrl()))")
    @Mapping(target = "commentCount", expression = "java(newsModel.getComments() != null ? (long) newsModel.getComments().size() : 0L)")
    NewsListItem toListItem(NewsModel newsModel);

    // List mappings
    List<NewsResponse> toResponseList(List<NewsModel> newsModels);

    List<NewsListItem> toListItemList(List<NewsModel> newsModels);

    // Pagination mapping
    @Mapping(target = "content", expression = "java(toListItemList(newsPage.getContent()))")
    @Mapping(target = "totalElements", source = "totalElements")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "number", source = "number")
    @Mapping(target = "numberOfElements", source = "numberOfElements")
    @Mapping(target = "first", source = "first")
    @Mapping(target = "last", source = "last")
    @Mapping(target = "empty", source = "empty")
    @Mapping(target = "pageable", source = "pageable")
    PagedNewsListResponse toPagedResponse(Page<NewsModel> newsPage);

    // Helper methods for conversions
    default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }

    default LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    default String uriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    default URI stringToUri(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return URI.create(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}