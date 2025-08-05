package com.dev.news.newsportal.service;

import com.dev.news.newsportal.model.UserModel;

import java.util.List;

public interface UserService {

    UserModel findById(Long id);

    List<UserModel> findAll();

    UserModel create(UserModel userModel);

    UserModel update(Long id, UserModel userModel);

    void delete(Long id);

    UserModel findByNickname(String nickname);

    UserModel findByEmail(String email);
}