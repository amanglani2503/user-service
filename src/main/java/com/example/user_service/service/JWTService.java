package com.example.user_service.service;

import com.example.user_service.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    // Generates JWT token with username and role as claims
    public String generateToken(String username, Role role) {
        try {
            logger.info("Generating token for username: {}", username);
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);

            String token = Jwts.builder()
                    .claims()
                    .add(claims)
                    .subject(username)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                    .and()
                    .signWith(getKey())
                    .compact();

            logger.debug("Token generated successfully for username: {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Error generating token for username {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Token generation failed");
        }
    }

    // Returns the signing key for JWT
    private SecretKey getKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Failed to decode JWT secret: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid JWT secret key");
        }
    }

    // Extracts username (subject) from token
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage(), e);
            return null;
        }
    }

    // Extracts a specific claim from the token using the provided function
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        try {
            Claims claims = extractAllClaims(token);
            return claimResolver.apply(claims);
        } catch (Exception e) {
            logger.error("Error extracting claim from token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract claim");
        }
    }

    // Parses and returns all claims from the token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Failed to parse claims from token: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid JWT token");
        }
    }

    // Validates the token with user details and expiration
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String userName = extractUsername(token);
            boolean valid = userName != null && userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.debug("Token validation for username {}: {}", userName, valid);
            return valid;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    // Checks if the token is expired
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage(), e);
            return true;
        }
    }

    // Extracts expiration time from the token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
