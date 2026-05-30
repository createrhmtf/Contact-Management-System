package com.cms;

import com.cms.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserEntityTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Initialize a fresh User object before each test
        user = new User();
    }

    @Test
    @DisplayName("Should create a User object with builder pattern and set all fields correctly")
    void testUserBuilderCreation() {
        // Arrange & Act: Create a User using builder pattern
        User newUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .passwordHash("securePassword123")
                .build();

        // Assert: Verify all fields are set correctly
        assertNotNull(newUser, "User object should not be null");
        assertEquals("John", newUser.getFirstName(), "First name should match");
        assertEquals("Doe", newUser.getLastName(), "Last name should match");
        assertEquals("john@example.com", newUser.getEmail(), "Email should match");
        assertEquals("1234567890", newUser.getPhoneNumber(), "Phone number should match");
        assertEquals("securePassword123", newUser.getPasswordHash(), "Password hash should match");
    }

    @Test
    @DisplayName("Should create a User with a non-null email")
    void testUserEmailIsNotNull() {
        // Arrange & Act: Create a User with a valid email using builder pattern
        User newUser = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .passwordHash("password123")
                .build();

        // Assert: Email should not be null
        assertNotNull(newUser.getEmail(), "Email should not be null");
        assertEquals("jane@example.com", newUser.getEmail(), "Email should match the provided value");
    }

    @Test
    @DisplayName("Should set and retrieve User fields using setter methods")
    void testUserSettersAndGetters() {
        // Arrange: Set values using setters
        user.setFirstName("Alice");
        user.setLastName("Johnson");
        user.setEmail("alice@example.com");
        user.setPhoneNumber("9876543210");
        user.setPasswordHash("myPassword456");

        // Act & Assert: Verify values using getters
        assertEquals("Alice", user.getFirstName(), "First name should be set correctly");
        assertEquals("Johnson", user.getLastName(), "Last name should be set correctly");
        assertEquals("alice@example.com", user.getEmail(), "Email should be set correctly");
        assertEquals("9876543210", user.getPhoneNumber(), "Phone number should be set correctly");
        assertEquals("myPassword456", user.getPasswordHash(), "Password hash should be set correctly");
    }

    @Test
    @DisplayName("Should create multiple User objects with different emails")
    void testCreateMultipleUsers() {
        // Arrange & Act: Create two different users
        User user1 = User.builder()
                .firstName("User")
                .lastName("One")
                .email("user1@example.com")
                .passwordHash("pass1")
                .build();

        User user2 = User.builder()
                .firstName("User")
                .lastName("Two")
                .email("user2@example.com")
                .passwordHash("pass2")
                .build();

        // Assert: Verify both users have different emails
        assertNotEquals(user1.getEmail(), user2.getEmail(), "Users should have different emails");
        assertTrue(user1.getEmail().contains("user1"), "User1 email should contain 'user1'");
        assertTrue(user2.getEmail().contains("user2"), "User2 email should contain 'user2'");
    }
}
