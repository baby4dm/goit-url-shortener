package edu.goit.urlshortener.model.response;

import lombok.Data;

@Data
public class UserResponse {
    private String username;
    private String password;
}