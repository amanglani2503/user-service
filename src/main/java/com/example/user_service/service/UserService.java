package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    // Fetch user by email or throw exception if not found
    public User getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new RuntimeException("User not found");
                });
    }

    // Extract Bearer token from Authorization header
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.debug("Authorization header found, extracting token.");
            return authHeader.substring(7);
        }
        logger.warn("Authorization header missing or invalid.");
        return null;
    }

    // Update user fields conditionally and save the user
    public User updateUser(Integer userId, User updatedUser) {
        logger.info("Updating user with ID: {}", userId);
        return userRepository.findById(userId).map(user -> {
            if (updatedUser.getName() != null) {
                user.setName(updatedUser.getName());
                logger.debug("Updated name for user ID {}: {}", userId, updatedUser.getName());
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
                logger.debug("Updated email for user ID {}: {}", userId, updatedUser.getEmail());
            }
            if (updatedUser.getPassword() != null) {
                String encodedPassword = new BCryptPasswordEncoder().encode(updatedUser.getPassword());
                user.setPassword(encodedPassword);
                logger.debug("Updated password for user ID {}", userId);
            }
            User savedUser = userRepository.save(user);
            logger.info("User updated successfully for ID: {}", userId);
            return savedUser;
        }).orElseThrow(() -> {
            logger.warn("User not found with ID: {}", userId);
            return new RuntimeException("User not found");
        });
    }
}
