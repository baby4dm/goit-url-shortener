package edu.goit.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goit.urlshortener.security.model.AuthRequest;
import edu.goit.urlshortener.security.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    //@MockBean
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest signupRequest;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));


    @BeforeEach
    void setUp() {
        signupRequest = new AuthRequest("testUser", "testPassword");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignupSuccess() throws Exception {
        // Arrange
        // Mocking the behavior of the userService to do nothing when createUser is called
        when(userService.registerUser(any(AuthRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.password").value("testPassword"));

        // Verify that the createUser method was called exactly once
        verify(userService, times(1)).registerUser(any(AuthRequest.class));
    }

    @Test
    void testSignupValidationFailure() throws Exception {
        // Arrange
        // Request with only the name (validation should fail)

        // Act & Assert
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("testUser")))
                .andExpect(status().isForbidden());

        // Verify that createUser method was not called since validation failed
        verify(userService, times(0)).registerUser(any(AuthRequest.class));
    }
}
