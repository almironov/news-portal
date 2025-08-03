package com.dev.news.newsportal.mapper;

import com.dev.news.newsportal.dto.request.CommentRequestDto;
import com.dev.news.newsportal.dto.response.CommentListItemDto;
import com.dev.news.newsportal.dto.response.CommentResponseDto;
import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "news.id", source = "newsId")
    @Mapping(target = "parentComment", ignore = true)
    Comment toEntity(CommentRequestDto dto);
    
    @Mapping(target = "replies", source = "replies")
    CommentResponseDto toResponseDto(Comment comment);
    
    @Mapping(target = "hasReplies", expression = "java(comment.getReplies() != null && !comment.getReplies().isEmpty())")
    CommentListItemDto toListItemDto(Comment comment);
    
    List<CommentListItemDto> toListItemDtoList(List<Comment> comments);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "news", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    void updateEntityFromDto(CommentRequestDto dto, @MappingTarget Comment comment);
    
    @AfterMapping
    default void setNewsAndParentComment(@MappingTarget Comment comment, CommentRequestDto dto) {
        // For toEntity method, we always set the news and parentComment
        // For updateEntityFromDto method, we only set them if they're not already set
        if (comment.getId() == null) {  // This is a new entity being created by toEntity
            if (dto.getNewsId() != null) {
                News news = new News();
                news.setId(dto.getNewsId());
                comment.setNews(news);
            }
            
            if (dto.getParentCommentId() != null) {
                Comment parentComment = new Comment();
                parentComment.setId(dto.getParentCommentId());
                comment.setParentComment(parentComment);
            }
        }
    }
}