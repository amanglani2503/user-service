package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.service.JWTService;
import com.example.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    // Returns user profile by extracting token from request and resolving email
    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(HttpServletRequest request) {
        try {
            logger.info("Fetching user profile from token");

            String token = userService.extractToken(request);
            if (token == null) {
                logger.warn("No token found in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = jwtService.extractUsername(token);
            logger.debug("Email extracted from token: {}", email);

            User user = userService.getUserByEmail(email);
            logger.info("User profile retrieved for email: {}", email);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error retrieving user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Updates user profile based on provided ID and updated data
    @PutMapping("/update-profile")
    public ResponseEntity<User> updateUserProfile(@PathVariable Integer id, @RequestBody User updatedUser) {
        try {
            logger.info("Updating user profile for ID: {}", id);
            User user = userService.updateUser(id, updatedUser);
            logger.info("User profile updated for ID: {}", id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error updating user profile for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
