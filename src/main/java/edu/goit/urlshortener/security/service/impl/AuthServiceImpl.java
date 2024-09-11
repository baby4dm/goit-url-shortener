package edu.goit.urlshortener.security.service.impl;

import edu.goit.urlshortener.security.jwt.JwtTokenUtil;
import edu.goit.urlshortener.security.model.AuthResponse;
import edu.goit.urlshortener.security.model.AuthRequest;
import edu.goit.urlshortener.security.service.AuthService;
import edu.goit.urlshortener.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AuthResponse login(AuthRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);

        return new AuthResponse(
                userDetails.getUsername(),
                userDetails.getAuthorities(),
                token
        );
    }

    @Override
    public AuthResponse refreshToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {

            String jwtToken = token.substring(7);

            String refreshedToken = jwtTokenUtil.refreshToken(jwtToken);
            UserDetails userDetails = userService.findByUsername(jwtTokenUtil.getUsernameFromToken(refreshedToken));

            return new AuthResponse(
                    userDetails.getUsername(),
                    userDetails.getAuthorities(),
                    refreshedToken
            );
        } else {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    @Override
    public String registerUser(AuthRequest request) {
        return userService.registerUser(request);
    }
}
