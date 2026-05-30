package com.cms.repository;

import com.cms.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Test saving a User and findByEmail returns it")
    void testSaveAndFindByEmail() {
        // Arrange
        User user = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .passwordHash("hashedPassword")
                .phoneNumber("1234567890")
                .build();
        
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("jane.doe@example.com");

        // Assert
        assertTrue(foundUser.isPresent(), "User should be found by email");
        assertEquals("Jane", foundUser.get().getFirstName());
    }

    @Test
    @DisplayName("Test existsByEmail returns true for existing email")
    void testExistsByEmailReturnsTrue() {
        // Arrange
        User user = User.builder()
                .firstName("Mark")
                .email("mark@example.com")
                .passwordHash("hashedPassword")
                .phoneNumber("0987654321")
                .build();
                
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail("mark@example.com");

        // Assert
        assertTrue(exists, "existsByEmail should return true for an existing email");
    }

    @Test
    @DisplayName("Test existsByEmail returns false for non-existing email")
    void testExistsByEmailReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nobody@example.com");

        // Assert
        assertFalse(exists, "existsByEmail should return false for an email that does not exist");
    }
}