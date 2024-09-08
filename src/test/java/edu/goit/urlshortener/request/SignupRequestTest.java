package edu.goit.urlshortener.request;

import edu.goit.urlshortener.model.request.SignupRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SignupRequestTest {

    private Validator validator;
    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        signupRequest = new SignupRequest();
    }

    @Test
    void testValidSignupRequest() {
        signupRequest.setUsername("testUser");
        signupRequest.setPassword("testPassword");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(signupRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidSignupRequest_MissingUsername() {
        signupRequest.setPassword("testPassword");

        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(signupRequest);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());  // Only one violation (missing username)
    }
}
