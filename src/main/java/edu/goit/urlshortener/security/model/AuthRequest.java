package edu.goit.urlshortener.security.model;

public record AuthRequest(
        String username,
        String password
) {
}
