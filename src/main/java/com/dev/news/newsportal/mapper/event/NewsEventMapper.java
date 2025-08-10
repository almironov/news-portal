package com.dev.news.newsportal.mapper.event;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.event.NewsCreatedEvent;
import com.dev.news.newsportal.event.NewsUpdatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

/**
 * MapStruct mapper for converting News entities to event objects.
 */
@Mapper(componentModel = "spring")
public interface NewsEventMapper {

    /**
     * Converts a News entity to a NewsCreatedEvent.
     *
     * @param news the News entity
     * @return NewsCreatedEvent with current timestamp
     */
    @Mapping(target = "newsId", source = "id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorNickname", source = "author.nickname")
    @Mapping(target = "eventTimestamp", expression = "java(java.time.LocalDateTime.now())")
    NewsCreatedEvent toNewsCreatedEvent(News news);

    /**
     * Converts a News entity to a NewsUpdatedEvent.
     *
     * @param news the News entity
     * @return NewsUpdatedEvent with current timestamp
     */
    @Mapping(target = "newsId", source = "id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorNickname", source = "author.nickname")
    @Mapping(target = "eventTimestamp", expression = "java(java.time.LocalDateTime.now())")
    NewsUpdatedEvent toNewsUpdatedEvent(News news);
}