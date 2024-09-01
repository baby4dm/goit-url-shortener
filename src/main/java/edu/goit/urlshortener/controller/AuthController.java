package edu.goit.urlshortener.controller;


import edu.goit.urlshortener.model.request.SignupRequest;
import edu.goit.urlshortener.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AuthController {
    @Autowired
    private final UserServiceImpl userService;


    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public String signup(@RequestBody SignupRequest signupRequest) {
        return userService.createUser(signupRequest);
    }


}
