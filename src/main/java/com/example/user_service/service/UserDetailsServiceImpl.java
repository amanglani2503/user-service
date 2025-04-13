package com.example.user_service.service;

import com.example.user_service.model.UserPrincipal;
import com.example.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    // Loads user by email (username) and wraps it in a UserPrincipal
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Attempting to load user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(user -> {
                    logger.info("User found for email: {}", email);
                    return new UserPrincipal(user);
                })
                .orElseThrow(() -> {
                    logger.warn("User not found for email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }
}
