package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    UserServiceImpl(UserRepository userRepository, UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserModel findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userEntityMapper.toModel(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserModel> findAll() {
        List<User> users = userRepository.findAll();
        return userEntityMapper.toModelList(users);
    }

    @Override
    public UserModel create(UserModel userModel) {
        validateUniqueFields(null, userModel.getNickname(), userModel.getEmail());

        // Convert domain model to entity
        User user = userEntityMapper.toEntity(userModel);
        user.setId(null); // Ensure it's a new entity

        // Save entity
        User savedUser = userRepository.save(user);

        // Convert back to domain model and return
        return userEntityMapper.toModel(savedUser);
    }

    @Override
    public UserModel update(Long id, UserModel userModel) {
        // Find existing user entity
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        validateUniqueFields(id, userModel.getNickname(), userModel.getEmail());

        // Update fields from domain model
        existingUser.setNickname(userModel.getNickname());
        existingUser.setEmail(userModel.getEmail());
        existingUser.setRole(userModel.getRole());

        // Save updated entity
        User updatedUser = userRepository.save(existingUser);

        // Convert back to domain model and return
        return userEntityMapper.toModel(updatedUser);
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
    public UserModel findByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nickname", nickname));
        return userEntityMapper.toModel(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserModel findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userEntityMapper.toModel(user);
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