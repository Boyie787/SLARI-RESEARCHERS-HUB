package slari.researchers.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtUtil — JWT Token Generator and Validator
 *
 * Enhanced with:
 * - Proper charset handling
 * - Role and userId in token claims
 * - Comprehensive logging
 * - Better error handling
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Build a cryptographic signing key from the secret string.
     * Uses UTF-8 charset and ensures minimum length.
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            log.error("JWT secret is too short ({} bytes). Must be at least 32 bytes.", keyBytes.length);
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token for a user email (simple version).
     * Used by AuthService for backward compatibility.
     *
     * @param email the user's email
     * @return signed JWT string
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate a JWT token with claims for a logged-in user.
     * This version includes role, userId, and name in the token claims.
     *
     * @param user the authenticated user (User entity)
     * @return signed JWT string
     */
    public String generateTokenWithClaims(slari.researchers.entity.User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        claims.put("name", user.getName());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the email address from a token's subject claim.
     */
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            log.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract a specific claim from the token.
     */
    public Object getClaimFromToken(String token, String claimName) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(claimName);
        } catch (Exception e) {
            log.error("Error extracting claim {} from token: {}", claimName, e.getMessage());
            return null;
        }
    }

    /**
     * Get user ID from token.
     */
    public Long getUserIdFromToken(String token) {
        Object userId = getClaimFromToken(token, "userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * Get user role from token.
     */
    public String getRoleFromToken(String token) {
        Object role = getClaimFromToken(token, "role");
        return role != null ? role.toString() : null;
    }

    /**
     * Validate a JWT token with detailed logging.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiry: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Get token expiration date.
     */
    public Date getExpirationDate(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (Exception e) {
            log.error("Error getting expiration date: {}", e.getMessage());
            return null;
        }
    }
}