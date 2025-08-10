package com.dev.news.newsportal.mapper.event;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.event.CommentCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting Comment entities to event objects.
 */
@Mapper(componentModel = "spring")
public interface CommentEventMapper {

    /**
     * Converts a Comment entity to a CommentCreatedEvent.
     *
     * @param comment the Comment entity
     * @return CommentCreatedEvent with current timestamp
     */
    @Mapping(target = "commentId", source = "id")
    @Mapping(target = "newsId", source = "news.id")
    @Mapping(target = "newsTitle", source = "news.title")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    @Mapping(target = "eventTimestamp", expression = "java(java.time.LocalDateTime.now())")
    CommentCreatedEvent toCommentCreatedEvent(Comment comment);
}