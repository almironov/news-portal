package com.dev.news.newsportal.service;

import com.dev.news.newsportal.dto.request.UserRequestDto;
import com.dev.news.newsportal.dto.response.UserResponseDto;
import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.UserMapper;
import com.dev.news.newsportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponseDto(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public UserResponseDto create(UserRequestDto dto) {
        validateUniqueFields(null, dto.getNickname(), dto.getEmail());
        
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDto(savedUser);
    }
    
    @Override
    public UserResponseDto update(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        validateUniqueFields(id, dto.getNickname(), dto.getEmail());
        
        userMapper.updateEntityFromDto(dto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }
    
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nickname", nickname));
        return userMapper.toResponseDto(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponseDto(user);
    }
    
    private void validateUniqueFields(Long id, String nickname, String email) {
        // Check if nickname is already taken by another user
        if (userRepository.existsByNickname(nickname)) {
            userRepository.findByNickname(nickname)
                    .filter(user -> !user.getId().equals(id))
                    .ifPresent(user -> {
                        throw new DuplicateResourceException("User", "nickname", nickname);
                    });
        }
        
        // Check if email is already taken by another user
        if (userRepository.existsByEmail(email)) {
            userRepository.findByEmail(email)
                    .filter(user -> !user.getId().equals(id))
                    .ifPresent(user -> {
                        throw new DuplicateResourceException("User", "email", email);
                    });
        }
    }
}