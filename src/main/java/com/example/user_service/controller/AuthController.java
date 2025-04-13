package com.example.user_service.controller;

import com.example.user_service.model.Role;
import com.example.user_service.model.User;
import com.example.user_service.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    // Handles user registration with default role assignment
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            logger.info("Received registration request for email: {}", user.getEmail());
            user.setRole(Role.valueOf("PASSENGER"));
            User registeredUser = authService.register(user);
            logger.info("User registered successfully with email: {}", registeredUser.getEmail());
            return ResponseEntity.ok(registeredUser);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid role provided during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid role specified.");
        } catch (Exception e) {
            logger.error("Error occurred during user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed due to an internal error.");
        }
    }

    // Handles user login and returns JWT if authenticated
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody User user) {
        try {
            logger.info("Login attempt for email: {}", user.getEmail());
            String token = authService.login(user);

            if (token == null) {
                logger.warn("Login failed for email: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
            }

            logger.info("Login successful for email: {}", user.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            logger.error("Error occurred during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Login failed due to an internal error"));
        }
    }
}
