package com.dev.news.newsportal.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test class demonstrating JUnit 5 parameterized tests and enhanced features.
 * This class showcases the JUnit configuration capabilities we've added to the project.
 */
@DisplayName("Validation Utils Tests")
class ValidationUtilsTest {

    /**
     * Example parameterized test using @ValueSource.
     * Demonstrates testing multiple values with a single test method.
     */
    @ParameterizedTest
    @DisplayName("Valid email addresses should be accepted")
    @ValueSource(strings = {
        "user@example.com",
        "test.email@domain.org", 
        "admin@newsportal.com",
        "user123@test-domain.co.uk"
    })
    void isValidEmail_withValidEmails_shouldReturnTrue(String email) {
        // Given & When
        boolean result = isValidEmail(email);
        
        // Then
        assertThat(result).isTrue();
    }

    /**
     * Example parameterized test using @NullAndEmptySource.
     * Demonstrates testing edge cases with null and empty values.
     */
    @ParameterizedTest
    @DisplayName("Null and empty email addresses should be rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void isValidEmail_withInvalidEmails_shouldReturnFalse(String email) {
        // Given & When
        boolean result = isValidEmail(email);
        
        // Then
        assertThat(result).isFalse();
    }

    /**
     * Example parameterized test using @CsvSource.
     * Demonstrates testing with multiple parameters and expected results.
     */
    @ParameterizedTest
    @DisplayName("Text length validation should work correctly")
    @CsvSource({
        "'Hello World', 5, 20, true",
        "'Hi', 5, 20, false",
        "'This is a very long text that exceeds the maximum length', 5, 20, false",
        "'Perfect', 5, 20, true",
        "'', 0, 10, true"
    })
    void isValidTextLength_withVariousInputs_shouldReturnExpectedResult(
            String text, int minLength, int maxLength, boolean expected) {
        // Given & When
        boolean result = isValidTextLength(text, minLength, maxLength);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }

    /**
     * Regular test method demonstrating display name customization.
     */
    @Test
    @DisplayName("Should validate nickname format correctly")
    void isValidNickname_withValidFormat_shouldReturnTrue() {
        // Given
        String validNickname = "user123";
        
        // When
        boolean result = isValidNickname(validNickname);
        
        // Then
        assertThat(result).isTrue();
    }

    // Helper methods for demonstration purposes
    // In a real application, these would be in a separate utility class

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    private boolean isValidTextLength(String text, int minLength, int maxLength) {
        if (text == null) {
            return false;
        }
        int length = text.length();
        return length >= minLength && length <= maxLength;
    }

    private boolean isValidNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false;
        }
        return nickname.matches("^[a-zA-Z0-9_]+$");
    }
}