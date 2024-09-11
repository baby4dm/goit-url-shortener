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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class UserServiceImplTest {
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
        AuthRequest authRequest = new AuthRequest("newUser", "password123");

        when(userRepository.existsByUsername("newUser")).thenReturn(false);  // Simulate that the user doesn't exist
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");  // Simulate password encoding

        String result = userService.registerUser(authRequest);

        assertEquals("newUser", result);

        verify(userRepository, times(1)).save(any(User.class));

    }

    @Test
    @Transactional
    void testCreateUserAlreadyExists() {
        AuthRequest authRequest = new AuthRequest("existingUser", "password123");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(authRequest);
        });

        assertEquals("User with username existingUser already exists", exception.getMessage());

        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void testFindByUsernameSuccess() {
        User mockUser = new User();
        mockUser.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(mockUser));

        User user = userService.findByUsername("testUser");

        assertNotNull(user);
        assertEquals("testUser", user.getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findByUsername("testUser"));
    }
}
