package com.dev.news.newsportal.mapper;

import com.dev.news.newsportal.dto.request.NewsRequestDto;
import com.dev.news.newsportal.dto.response.NewsListItemDto;
import com.dev.news.newsportal.dto.response.NewsResponseDto;
import com.dev.news.newsportal.entity.News;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface NewsMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(source = "authorId", target = "author.id")
    News toEntity(NewsRequestDto dto);
    
    @Mapping(source = "author", target = "author")
    @Mapping(target = "commentCount", expression = "java(news.getComments() != null ? news.getComments().size() : 0)")
    NewsResponseDto toResponseDto(News news);
    
    @Mapping(source = "author", target = "author")
    @Mapping(target = "commentCount", expression = "java(news.getComments() != null ? news.getComments().size() : 0)")
    NewsListItemDto toListItemDto(News news);
    
    List<NewsListItemDto> toListItemDtoList(List<News> newsList);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "author", ignore = true)
    void updateEntityFromDto(NewsRequestDto dto, @MappingTarget News news);
}