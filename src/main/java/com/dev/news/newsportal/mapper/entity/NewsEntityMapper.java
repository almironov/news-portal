package com.dev.news.newsportal.mapper.entity;

import com.dev.news.newsportal.entity.News;
import com.dev.news.newsportal.model.NewsModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserEntityMapper.class, CommentEntityMapper.class})
public interface NewsEntityMapper {

    NewsModel toModel(News entity);

    News toEntity(NewsModel model);

    List<NewsModel> toModelList(List<News> entities);

    List<News> toEntityList(List<NewsModel> models);
}