package edu.goit.urlshortener.service;

import edu.goit.urlshortener.repo.UserRepository;
import edu.goit.urlshortener.security.model.AuthRequest;
import edu.goit.urlshortener.security.model.User;
import edu.goit.urlshortener.security.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private AuthRequest signupRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        signupRequest = new AuthRequest("testUser", "testPassword");

    }


    @Test
    void testCreateUserSuccess() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("newUser", "password123");

        // Mocking repository and encoder behavior
        when(userRepository.existsByUsername("newUser")).thenReturn(false);  // Simulate that the user doesn't exist
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");  // Simulate password encoding

        // Act
        String result = userService.registerUser(authRequest);

        // Assert
        assertEquals("newUser", result);  // Assert that the username is returned

        // Verify that the save method was called with a user object
        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    @Transactional
    void testCreateUserAlreadyExists() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("existingUser", "password123");

        // Mocking repository to simulate that the user already exists
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(authRequest);
        });

        assertEquals("User with username existingUser already exists", exception.getMessage());

        // Verify that the save method was not called
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testFindByUsernameSuccess() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        // Act
        User user = userService.findByUsername("testUser");

        // Assert
        assertNotNull(user);
        assertEquals("testUser", user.getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.findByUsername("testUser"));
    }
}
