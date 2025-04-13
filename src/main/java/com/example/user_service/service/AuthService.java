package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    // Handles user registration with encrypted password
    public User register(User user) {
        try {
            logger.info("Registering user with email: {}", user.getEmail());
            user.setPassword(encoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with email: {}", savedUser.getEmail());
            return savedUser;
        } catch (Exception e) {
            logger.error("Error during registration for email {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Registration failed");
        }
    }

    // Authenticates user and generates JWT on success
    public String login(User user) {
        try {
            logger.info("Attempting authentication for email: {}", user.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                Optional<User> extractedUser = userRepository.findByEmail(user.getEmail());
                if (extractedUser.isPresent()) {
                    String token = jwtService.generateToken(user.getEmail(), extractedUser.get().getRole());
                    logger.info("JWT token generated successfully for email: {}", user.getEmail());
                    return token;
                } else {
                    logger.warn("User not found after authentication for email: {}", user.getEmail());
                }
            }
        } catch (Exception e) {
            logger.error("Login failed for email {}: {}", user.getEmail(), e.getMessage(), e);
        }
        return null;
    }
}
