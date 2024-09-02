package edu.goit.urlshortener.model.request;

import lombok.Data;

@Data
public class SignupRequest {
    private String password;
    private String username;
}
