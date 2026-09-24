package slari.researchers.security;

import slari.researchers.entity.User;
import slari.researchers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("No user found with email: " + email);
                });

        // Check if user is active
        if (!user.isActive()) {
            log.warn("User account is deactivated: {}", email);
            throw new UsernameNotFoundException("User account is deactivated");
        }

        // Build authorities
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        log.debug("User loaded successfully: {} with role: {}", email, user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),      // enabled
                true,                 // accountNonExpired
                true,                 // credentialsNonExpired
                true,                 // accountNonLocked
                authorities
        );
    }

    /**
     * Get user entity from security context (for controllers).
     * Note: This assumes the User object is attached to the Authentication.
     */
    public User getCurrentUser() {
        // This would be implemented with a custom authentication token
        // that stores the full User object
        return null;
    }
}