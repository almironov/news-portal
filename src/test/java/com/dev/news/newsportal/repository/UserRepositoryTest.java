package com.dev.news.newsportal.repository;

import com.dev.news.newsportal.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateAndFindById() {
        // Create a user
        User user = User.builder()
                .nickname("testuser")
                .email("test@example.com")
                .role("REGISTERED_USER")
                .build();

        // Save the user
        User savedUser = userRepository.save(user);

        // Find the user by ID
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert that the user was found and has the correct properties
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getRole()).isEqualTo("REGISTERED_USER");
    }

    @Test
    public void testFindByNickname() {
        // Create a user
        User user = User.builder()
                .nickname("johndoe")
                .email("john@example.com")
                .role("ADMIN")
                .build();

        // Save the user using EntityManager
        entityManager.persist(user);
        entityManager.flush();

        // Find the user by nickname
        Optional<User> foundUser = userRepository.findByNickname("johndoe");

        // Assert that the user was found and has the correct properties
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
        assertThat(foundUser.get().getRole()).isEqualTo("ADMIN");
    }

    @Test
    public void testFindByEmail() {
        // Create a user
        User user = User.builder()
                .nickname("janedoe")
                .email("jane@example.com")
                .role("GUEST")
                .build();

        // Save the user
        userRepository.save(user);

        // Find the user by email
        Optional<User> foundUser = userRepository.findByEmail("jane@example.com");

        // Assert that the user was found and has the correct properties
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo("janedoe");
        assertThat(foundUser.get().getRole()).isEqualTo("GUEST");
    }

    @Test
    public void testUpdateUser() {
        // Create a user
        User user = User.builder()
                .nickname("updateuser")
                .email("update@example.com")
                .role("REGISTERED_USER")
                .build();

        // Save the user
        User savedUser = userRepository.save(user);

        // Update the user
        savedUser.setRole("ADMIN");
        userRepository.save(savedUser);

        // Find the user by ID
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert that the user was updated
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo("ADMIN");
    }

    @Test
    public void testDeleteUser() {
        // Create a user
        User user = User.builder()
                .nickname("deleteuser")
                .email("delete@example.com")
                .role("REGISTERED_USER")
                .build();

        // Save the user
        User savedUser = userRepository.save(user);

        // Delete the user
        userRepository.delete(savedUser);

        // Try to find the user by ID
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert that the user was deleted
        assertThat(foundUser).isEmpty();
    }

    @Test
    public void testExistsByNickname() {
        // Create a user
        User user = User.builder()
                .nickname("existsuser")
                .email("exists@example.com")
                .role("REGISTERED_USER")
                .build();

        // Save the user
        userRepository.save(user);

        // Check if user exists by nickname
        boolean exists = userRepository.existsByNickname("existsuser");
        boolean notExists = userRepository.existsByNickname("nonexistentuser");

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    public void testExistsByEmail() {
        // Create a user
        User user = User.builder()
                .nickname("emailuser")
                .email("emailexists@example.com")
                .role("REGISTERED_USER")
                .build();

        // Save the user
        userRepository.save(user);

        // Check if user exists by email
        boolean exists = userRepository.existsByEmail("emailexists@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}