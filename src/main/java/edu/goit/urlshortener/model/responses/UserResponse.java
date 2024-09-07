package edu.goit.urlshortener.model.responses;

import lombok.Data;

@Data
public class UserResponse {
    private String username;
    private String password;
}