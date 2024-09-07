package edu.goit.urlshortener.security.service;

import edu.goit.urlshortener.security.model.AuthRequest;
import edu.goit.urlshortener.security.model.User;

public interface UserService {
    User findByUsername(String username);

    String registerUser(AuthRequest request);
}
