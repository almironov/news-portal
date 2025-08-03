package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.UserRequestDto;
import com.dev.news.newsportal.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    
    UserResponseDto findById(Long id);
    
    List<UserResponseDto> findAll();
    
    UserResponseDto create(UserRequestDto dto);
    
    UserResponseDto update(Long id, UserRequestDto dto);
    
    void delete(Long id);
    
    UserResponseDto findByNickname(String nickname);
    
    UserResponseDto findByEmail(String email);
}