package edu.goit.urlshortener.model.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}