package com.dev.news.newsportal.mapper;

import com.dev.news.newsportal.dto.request.UserRequestDto;
import com.dev.news.newsportal.dto.response.UserResponseDto;
import com.dev.news.newsportal.dto.response.UserSummaryDto;
import com.dev.news.newsportal.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "news", ignore = true)
    User toEntity(UserRequestDto dto);
    
    UserResponseDto toResponseDto(User user);
    
    UserSummaryDto toSummaryDto(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "news", ignore = true)
    void updateEntityFromDto(UserRequestDto dto, @MappingTarget User user);
}