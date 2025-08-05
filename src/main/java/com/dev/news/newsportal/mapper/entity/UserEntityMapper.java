package com.dev.news.newsportal.mapper.entity;

import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserModel toModel(User entity);

    @Mapping(target = "news", ignore = true)
    User toEntity(UserModel model);

    List<UserModel> toModelList(List<User> entities);

    List<User> toEntityList(List<UserModel> models);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "news", ignore = true)
    void updateEntity(@MappingTarget User target, UserModel source);
}