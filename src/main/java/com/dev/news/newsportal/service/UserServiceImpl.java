package com.dev.news.newsportal.service;

import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.exception.DuplicateResourceException;
import com.dev.news.newsportal.exception.ResourceNotFoundException;
import com.dev.news.newsportal.mapper.entity.UserEntityMapper;
import com.dev.news.newsportal.model.UserModel;
import com.dev.news.newsportal.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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
        log.debug("Finding user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });
        log.info("Successfully retrieved user with id: {}", id);
        return userEntityMapper.toModel(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserModel> findAll() {
        log.debug("Finding all users");
        List<User> users = userRepository.findAll();
        log.info("Successfully retrieved {} users", users.size());
        return userEntityMapper.toModelList(users);
    }

    @Override
    public UserModel create(UserModel userModel) {
        log.debug("Creating new user with nickname: {}", userModel.getNickname());
        validateUniqueFields(null, userModel.getNickname(), userModel.getEmail());

        // Convert domain model to entity
        User user = userEntityMapper.toEntity(userModel);
        user.setId(null); // Ensure it's a new entity

        // Save entity
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {} and nickname: {}", savedUser.getId(), savedUser.getNickname());

        // Convert back to domain model and return
        return userEntityMapper.toModel(savedUser);
    }

    @Override
    public UserModel update(Long id, UserModel userModel) {
        log.debug("Updating user with id: {} and nickname: {}", id, userModel.getNickname());
        
        // Find existing user entity
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {} for update", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        validateUniqueFields(id, userModel.getNickname(), userModel.getEmail());

        // Use UserEntityMapper to update properties, preserving id and news
        userEntityMapper.updateEntity(existingUser, userModel);

        // Save updated entity
        User updatedUser = userRepository.save(existingUser);
        log.info("Successfully updated user with id: {} and nickname: {}", updatedUser.getId(), updatedUser.getNickname());

        // Convert back to domain model and return
        return userEntityMapper.toModel(updatedUser);
    }

    @Override
    public void delete(Long id) {
        log.debug("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User not found with id: {} for deletion", id);
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        log.info("Successfully deleted user with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserModel findByNickname(String nickname) {
        log.debug("Finding user by nickname: {}", nickname);
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> {
                    log.warn("User not found with nickname: {}", nickname);
                    return new ResourceNotFoundException("User", "nickname", nickname);
                });
        log.info("Successfully retrieved user with nickname: {}", nickname);
        return userEntityMapper.toModel(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserModel findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });
        log.info("Successfully retrieved user with email: {}", email);
        return userEntityMapper.toModel(user);
    }

    private void validateUniqueFields(Long id, String nickname, String email) {
        log.debug("Validating unique fields for user - nickname: {}, email: {}", nickname, email);
        
        // Check if nickname is already taken by another user
        if (userRepository.existsByNickname(nickname)) {
            userRepository.findByNickname(nickname)
                    .filter(user -> !user.getId().equals(id))
                    .ifPresent(user -> {
                        log.warn("Nickname already exists: {}", nickname);
                        throw new DuplicateResourceException("User", "nickname", nickname);
                    });
        }

        // Check if email is already taken by another user
        if (userRepository.existsByEmail(email)) {
            userRepository.findByEmail(email)
                    .filter(user -> !user.getId().equals(id))
                    .ifPresent(user -> {
                        log.warn("Email already exists: {}", email);
                        throw new DuplicateResourceException("User", "email", email);
                    });
        }
        
        log.debug("Unique field validation passed for nickname: {} and email: {}", nickname, email);
    }
}