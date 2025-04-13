package com.example.user_service.service;

import com.example.user_service.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTServiceTest {

    private JWTService jwtService;

    private final String secret = "U29tZVN1cGVyU2VjcmV0S2V5MTIzNDU2U29tZVN1cGVyU2VjcmV0S2V5MTIzNDU2"; // base64 48+ chars

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        // Inject secretKey manually
        jwtService.getClass().getDeclaredFields(); // to satisfy code editors
        try {
            var field = JWTService.class.getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(jwtService, secret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject secret key", e);
        }
    }

    @Test
    void testGenerateToken_ShouldCreateValidJwt() {
        String token = jwtService.generateToken("testuser", Role.ADMIN);
        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testExtractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken("johndoe", Role.PASSENGER);
        String username = jwtService.extractUsername(token);
        assertEquals("johndoe", username);
    }

    @Test
    void testValidateToken_ShouldReturnTrueForValidToken() {
        String username = "janedoe";
        String token = jwtService.generateToken(username, Role.ADMIN);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);

        boolean isValid = jwtService.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_ShouldReturnFalseForInvalidUser() {
        String token = jwtService.generateToken("someone", Role.ADMIN);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("different");

        boolean isValid = jwtService.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ShouldReturnFalseIfExpired() throws InterruptedException {
        // Short expiry version to simulate expiry
        try {
            var field = JWTService.class.getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(jwtService, secret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String token = Jwts.builder()
                .subject("expireTest")
                .expiration(new Date(System.currentTimeMillis() - 1000)) // expired
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .signWith(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret)))
                .compact();

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("expireTest");

        boolean valid = jwtService.validateToken(token, userDetails);
        assertFalse(valid);
    }
}
