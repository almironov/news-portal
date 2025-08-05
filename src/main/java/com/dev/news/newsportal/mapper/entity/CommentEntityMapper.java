package com.dev.news.newsportal.mapper.entity;

import com.dev.news.newsportal.entity.Comment;
import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.model.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentEntityMapper {

    @Mapping(target = "newsId", source = "news.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    CommentModel toModel(Comment entity);

    @Mapping(target = "news", expression = "java(createNewsReference(model.getNewsId()))")
    @Mapping(target = "parentComment", expression = "java(createCommentReference(model.getParentCommentId()))")
    Comment toEntity(CommentModel model);

    List<CommentModel> toModelList(List<Comment> entities);

    List<Comment> toEntityList(List<CommentModel> models);

    default News createNewsReference(Long newsId) {
        if (newsId == null) {
            return null;
        }
        News news = new News();
        news.setId(newsId);
        return news;
    }

    default Comment createCommentReference(Long commentId) {
        if (commentId == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentId);
        return comment;
    }
}