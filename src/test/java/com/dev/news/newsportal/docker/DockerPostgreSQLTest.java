package com.dev.news.newsportal.docker;

import com.dev.news.newsportal.entity.User;
import com.dev.news.newsportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test verifies that the PostgreSQL Docker container is running correctly
 * and that the application can connect to it.
 * 
 * Before running this test, make sure to start the Docker container using:
 * docker-compose up -d
 */
@SpringBootTest
@ActiveProfiles("pgsql")
public class DockerPostgreSQLTest {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Test that we can connect to the PostgreSQL database running in Docker.
     */
    @Test
    public void testDatabaseConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            System.out.println("[DEBUG_LOG] Successfully connected to: " + 
                    connection.getMetaData().getDatabaseProductName() + " " +
                    connection.getMetaData().getDatabaseProductVersion());
        }
    }
    
    /**
     * Test that we can perform basic CRUD operations on the database.
     */
    @Test
    public void testDatabaseOperations() {
        // Create a test user
        User user = User.builder()
                .nickname("dockertest")
                .email("docker@test.com")
                .role("REGISTERED_USER")
                .build();
        
        // Save the user
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        
        // Retrieve the user
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getNickname()).isEqualTo("dockertest");
        
        // Update the user
        foundUser.get().setNickname("dockertestupdated");
        User updatedUser = userRepository.save(foundUser.get());
        assertThat(updatedUser.getNickname()).isEqualTo("dockertestupdated");
        
        // Delete the user
        userRepository.delete(updatedUser);
        Optional<User> deletedUser = userRepository.findById(updatedUser.getId());
        assertThat(deletedUser).isEmpty();
        
        System.out.println("[DEBUG_LOG] Successfully performed CRUD operations on PostgreSQL database");
    }
}