package edu.goit.urlshortener.service.impl;

import edu.goit.urlshortener.model.dto.SignupRequest;
import edu.goit.urlshortener.repo.RoleRepository;
import edu.goit.urlshortener.repo.UserRepository;
import edu.goit.urlshortener.security.model.User;
import edu.goit.urlshortener.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return "User already exists: " + signupRequest.getUsername();
        }
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        roleRepository.findByName("ROLE_USER").ifPresent(user::addRole);
        userRepository.save(user);
        return "User created: " + user.getUsername();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsernameFetchRoles(username).orElseThrow();
    }
}
