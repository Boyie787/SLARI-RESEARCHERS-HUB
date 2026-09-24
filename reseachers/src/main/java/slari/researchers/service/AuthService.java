package slari.researchers.service;

import slari.researchers.dto.request.*;
import slari.researchers.dto.response.AuthResponse;
import slari.researchers.entity.User;
import slari.researchers.enums.Role;
import slari.researchers.exception.ResourceNotFoundException;
import slari.researchers.repository.UserRepository;
import slari.researchers.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    private final Map<String, String[]> resetTokenStore = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> loginAttempts = new ConcurrentHashMap<>();

    private static final long RESET_TOKEN_EXPIRY_MS = 15 * 60 * 1000L;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_WINDOW_MS = 15 * 60 * 1000L;
    private static final long TOKEN_EXPIRY_SECONDS = 86400; // 24 hours

    /* ══════════════════════════════════════
       REGISTER
       ══════════════════════════════════════ */

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists.");
        }

        // Validate role - only allow FARMER registration from public endpoint
        Role role = request.getRole();
        if (role == null || role == Role.ADMIN || role == Role.RESEARCHER || role == Role.EXTENSION) {
            role = Role.FARMER; // Default to farmer
            log.warn("User attempted to register with invalid role, defaulting to FARMER");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .location(request.getLocation())
                .institute(request.getInstitute())
                .phone(request.getPhone())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail());

        log.info("New user registered: {} ({})", saved.getEmail(), saved.getRole());

        return buildAuthResponse(saved, token);
    }

    /* ══════════════════════════════════════
       LOGIN
       ══════════════════════════════════════ */

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        // Rate limiting
        if (isRateLimited(email)) {
            throw new RuntimeException("Too many login attempts. Please wait 15 minutes.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            // Clear attempts on success
            loginAttempts.remove(email);

        } catch (Exception e) {
            recordFailedAttempt(email);
            log.warn("Failed login attempt for: {}", email);
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!user.isActive()) {
            throw new RuntimeException("Your account has been deactivated. Please contact the administrator.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("User logged in: {} ({})", user.getEmail(), user.getRole());

        return buildAuthResponse(user, token);
    }

    /* ══════════════════════════════════════
       TOKEN REFRESH  ← ADDED THIS METHOD
       ══════════════════════════════════════ */

    /**
     * Refresh an existing token.
     * Generates a new token with extended expiry.
     */
    public AuthResponse refreshToken(String oldToken) {
        if (isTokenBlacklisted(oldToken)) {
            throw new RuntimeException("Token is blacklisted. Please login again.");
        }

        if (!jwtUtil.validateToken(oldToken)) {
            throw new RuntimeException("Invalid token. Please login again.");
        }

        String email = jwtUtil.getEmailFromToken(oldToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        // Blacklist old token
        tokenBlacklistService.blacklist(oldToken);

        // Generate new token
        String newToken = jwtUtil.generateToken(user.getEmail());

        log.info("Token refreshed for user: {}", email);

        return buildAuthResponse(user, newToken);
    }

    /* ══════════════════════════════════════
       LOGOUT
       ══════════════════════════════════════ */

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            tokenBlacklistService.blacklist(token);
            log.info("User logged out");
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistService.isBlacklisted(token);
    }

    /* ══════════════════════════════════════
       VALIDATE TOKEN
       ══════════════════════════════════════ */

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        if (isTokenBlacklisted(token)) return false;
        if (!jwtUtil.validateToken(token)) return false;

        try {
            String email = jwtUtil.getEmailFromToken(token);
            if (email == null) return false;
            User user = userRepository.findByEmail(email).orElse(null);
            return user != null && user.isActive();
        } catch (Exception e) {
            log.warn("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /* ══════════════════════════════════════
       CHANGE PASSWORD
       ══════════════════════════════════════ */

    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from your current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", userEmail);
    }

    /* ══════════════════════════════════════
       FORGOT PASSWORD
       ══════════════════════════════════════ */

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            log.warn("Password reset requested for unregistered email: {}", request.getEmail());
            return "If this email is registered, a reset link has been sent.";
        }

        String resetToken = UUID.randomUUID().toString();
        long expiry = System.currentTimeMillis() + RESET_TOKEN_EXPIRY_MS;

        resetTokenStore.put(resetToken, new String[]{user.getEmail(), String.valueOf(expiry)});

        log.info("Password reset token generated for: {}", user.getEmail());

        // TODO: In production, send email here
        // mailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        return resetToken; // REMOVE in production
    }

    /* ══════════════════════════════════════
       RESET PASSWORD
       ══════════════════════════════════════ */

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String[] tokenData = resetTokenStore.get(request.getResetToken());

        if (tokenData == null) {
            throw new RuntimeException("Invalid or expired reset token.");
        }

        long expiry = Long.parseLong(tokenData[1]);
        if (System.currentTimeMillis() > expiry) {
            resetTokenStore.remove(request.getResetToken());
            throw new RuntimeException("Reset token has expired.");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Passwords do not match.");
        }

        String email = tokenData[0];
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokenStore.remove(request.getResetToken());

        log.info("Password successfully reset for: {}", email);
    }

    /* ══════════════════════════════════════
       DELETE ACCOUNT  ← ADDED THIS METHOD
       ══════════════════════════════════════ */

    /**
     * Delete user's own account (soft delete).
     * Sets active = false.
     */
    @Transactional
    public void deleteAccount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Soft delete - deactivate account
        user.setActive(false);
        userRepository.save(user);

        log.info("User account deactivated: {}", userEmail);
    }

    /* ══════════════════════════════════════
       SCHEDULED CLEANUP
       ══════════════════════════════════════ */

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredResetTokens() {
        int removed = 0;
        Iterator<Map.Entry<String, String[]>> iterator = resetTokenStore.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> entry = iterator.next();
            long expiry = Long.parseLong(entry.getValue()[1]);
            if (System.currentTimeMillis() > expiry) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("Cleaned up {} expired reset tokens", removed);
        }
    }

    /* ══════════════════════════════════════
       RATE LIMITING HELPERS
       ══════════════════════════════════════ */

    private boolean isRateLimited(String email) {
        List<Long> attempts = loginAttempts.computeIfAbsent(email, k -> new ArrayList<>());
        long now = System.currentTimeMillis();
        attempts.removeIf(time -> now - time > LOGIN_WINDOW_MS);
        return attempts.size() >= MAX_LOGIN_ATTEMPTS;
    }

    private void recordFailedAttempt(String email) {
        List<Long> attempts = loginAttempts.computeIfAbsent(email, k -> new ArrayList<>());
        attempts.add(System.currentTimeMillis());
    }

    /* ══════════════════════════════════════
       PRIVATE HELPERS
       ══════════════════════════════════════ */

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRY_SECONDS)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .institute(user.getInstitute())
                .location(user.getLocation())
                .phone(user.getPhone())
                .build();
    }
}