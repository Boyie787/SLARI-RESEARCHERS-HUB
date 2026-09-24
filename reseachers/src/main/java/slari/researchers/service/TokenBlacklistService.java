package slari.researchers.service;   // ← your actual package

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenBlacklistService — manages invalidated JWT tokens
 *
 * Extracted into its own @Service to break the circular dependency:
 *   SecurityConfig → JwtAuthFilter → AuthService → SecurityConfig
 *
 * Both JwtAuthFilter and AuthService now inject this lightweight
 * service instead of depending on each other.
 *
 * Production: replace the in-memory Set with Redis for persistence
 * across server restarts and multiple instances.
 */
@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    /** Add a token to the blacklist — called on logout */
    public void blacklist(String token) {
        if (token != null && !token.isBlank()) {
            blacklist.add(token);
        }
    }

    /** Check if a token is blacklisted — called on every request */
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}