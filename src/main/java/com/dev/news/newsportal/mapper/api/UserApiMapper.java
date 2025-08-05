package com.dev.news.newsportal.mapper.api;

import com.dev.news.newsportal.api.model.news.UserSummary;
import com.dev.news.newsportal.api.model.users.UserRequest;
import com.dev.news.newsportal.api.model.users.UserResponse;
import com.dev.news.newsportal.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserApiMapper {

    // UserRequest to UserModel
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(userRequest.getRole() != null ? userRequest.getRole().getValue() : null)")
    UserModel toModel(UserRequest userRequest);

    // UserModel to UserResponse
    UserResponse toResponse(UserModel userModel);

    // UserModel to UserSummary
    UserSummary toSummary(UserModel userModel);

    // List mappings
    List<UserResponse> toResponseList(List<UserModel> userModels);

    List<UserSummary> toSummaryList(List<UserModel> userModels);
}