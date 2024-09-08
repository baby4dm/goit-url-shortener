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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
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

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Test
    void testCreateUserSuccess() {
        // Arrange
        when(userRepository.existsByUsername(any())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        // Act
        User mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setPassword("encodedPassword");

        String userName = userService.registerUser(signupRequest);

        // Assert
        assertNotNull(userName);
        assertEquals(userName, userRepository.findByUsername(userName).get().getUsername());
        verify(userRepository).findByUsername(userName);
        verify(userRepository).save(mockUser);
    }

    @Test
    @Transactional
    void testCreateUserAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername(userService.registerUser(signupRequest))).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(signupRequest));
        assertEquals("User with username testUser already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
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
