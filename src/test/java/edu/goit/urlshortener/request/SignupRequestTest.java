package edu.goit.urlshortener.request;

import edu.goit.urlshortener.security.model.AuthRequest;
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
    private AuthRequest signupRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        signupRequest = new AuthRequest("username", "password");
    }

    @Test
    void testValidSignupRequest() {

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(signupRequest);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidSignupRequest_MissingUsername() {

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(signupRequest);
        assertTrue(violations.isEmpty());
    }
}
