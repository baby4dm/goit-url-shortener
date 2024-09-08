package edu.goit.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goit.urlshortener.model.request.SignupRequest;
import edu.goit.urlshortener.model.response.UserResponse;
import edu.goit.urlshortener.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    private SignupRequest signupRequest;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));


    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testUser");
        signupRequest.setPassword("testPassword");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignupSuccess() throws Exception {
        // Arrange
        // Mocking the behavior of the userService to do nothing when createUser is called
        when(userService.createUser(any(SignupRequest.class))).thenReturn(null);

        // Expected UserResponse after successful signup
        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setUsername("testUser");
        expectedResponse.setPassword("testPassword");

        // Act & Assert
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.password").value("testPassword"));

        // Verify that the createUser method was called exactly once
        verify(userService, times(1)).createUser(any(SignupRequest.class));
    }

    @Test
    void testSignupValidationFailure() throws Exception {
        // Arrange
        SignupRequest invalidRequest = new SignupRequest();  // Request with only the name (validation should fail)
        invalidRequest.setUsername("testUser");

        // Act & Assert
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Verify that createUser method was not called since validation failed
        verify(userService, times(0)).createUser(any(SignupRequest.class));
    }
}
