package edu.goit.urlshortener.security.service;

import edu.goit.urlshortener.security.model.AuthResponse;
import edu.goit.urlshortener.security.model.AuthRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    AuthResponse login(AuthRequest loginRequest);

    AuthResponse refreshToken(String token);

    String registerUser(AuthRequest request);
}
