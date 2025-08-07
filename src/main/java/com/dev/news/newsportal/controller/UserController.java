package com.dev.news.newsportal.controller;

import com.dev.news.newsportal.api.model.users.UserRequest;
import com.dev.news.newsportal.api.model.users.UserResponse;
import com.dev.news.newsportal.api.users.UsersApi;
import com.dev.news.newsportal.mapper.api.UserApiMapper;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
class UserController implements UsersApi {

    private final UserService userService;
    private final UserApiMapper userApiMapper;

    UserController(UserService userService, UserApiMapper userApiMapper) {
        this.userService = userService;
        this.userApiMapper = userApiMapper;
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserModel> userModels = userService.findAll();
        List<UserResponse> userResponses = userApiMapper.toResponseList(userModels);
        return ResponseEntity.ok(userResponses);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        UserModel userModel = userService.findById(id);
        UserResponse userResponse = userApiMapper.toResponse(userModel);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        // Convert DTO to domain model
        UserModel userModel = userApiMapper.toModel(userRequest);

        // Create user
        UserModel createdUserModel = userService.create(userModel);

        // Convert back to DTO
        UserResponse userResponse = userApiMapper.toResponse(createdUserModel);

        // Create location header
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(userResponse);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(Long id, UserRequest userRequest) {
        // Convert DTO to domain model
        UserModel userModel = userApiMapper.toModel(userRequest);

        // Update user
        UserModel updatedUserModel = userService.update(id, userModel);

        // Convert back to DTO
        UserResponse userResponse = userApiMapper.toResponse(updatedUserModel);

        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserResponse> getUserByNickname(String nickname) {
        UserModel userModel = userService.findByNickname(nickname);
        UserResponse userResponse = userApiMapper.toResponse(userModel);
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<UserResponse> getUserByEmail(String email) {
        UserModel userModel = userService.findByEmail(email);
        UserResponse userResponse = userApiMapper.toResponse(userModel);
        return ResponseEntity.ok(userResponse);
    }
}