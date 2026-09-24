package slari.researchers.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Role — User Role Enum
 *
 * Defines every role a user can have on the SLARI platform.
 * Stored in the `users` table as a VARCHAR column.
 *
 * Used by:
 *   - User entity           → each user has exactly one role
 *   - SecurityConfig        → role-based URL access rules
 *   - @PreAuthorize         → method-level security on service/controller
 *   - NAV_CONFIG (frontend) → determines which sidebar links appear
 *
 * Spring Security requires the "ROLE_" prefix when using
 * hasRole() checks, but we store clean names here (FARMER, not ROLE_FARMER).
 * The UserDetailsServiceImpl converts them with "ROLE_" at login time.
 */
public enum Role {
    /** End users — browse, bookmark, and rate varieties */
    FARMER,

    /** Agricultural scientists — upload and manage variety research */
    RESEARCHER,

    /** Field agents — recommend varieties to specific farmers */
    EXTENSION,

    /** Platform administrators — approve submissions, manage users */
    ADMIN,

    /** Unauthenticated users — can only browse published varieties */
    GUEST;

    /**
     * Returns the Spring Security authority format (ROLE_ prefix).
     * Used when building UserDetails for authentication.
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Creates a SimpleGrantedAuthority for this role.
     * Used in SecurityConfig and UserDetailsService.
     */
    public SimpleGrantedAuthority asGrantedAuthority() {
        return new SimpleGrantedAuthority(getAuthority());
    }

    /**
     * Check if this role can access admin-only endpoints.
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this role can create/update varieties.
     */
    public boolean canCreateVarieties() {
        return this == RESEARCHER || this == ADMIN;
    }

    /**
     * Check if this role can approve/reject varieties.
     */
    public boolean canModerateVarieties() {
        return this == ADMIN;
    }

    /**
     * Check if this role can recommend varieties to farmers.
     */
    public boolean canRecommend() {
        return this == EXTENSION || this == ADMIN;
    }

    /**
     * Check if this role can access full platform features.
     */
    public boolean isAuthenticated() {
        return this != GUEST;
    }

    /**
     * Check if this role requires registration.
     */
    public boolean requiresRegistration() {
        return this != GUEST;
    }

    /**
     * Helper to check if role is an extension officer.
     */
    public boolean isExtensionOfficer() {
        return this == EXTENSION;
    }

    /**
     * Helper to check if role is a farmer.
     */
    public boolean isFarmer() {
        return this == FARMER;
    }

    /**
     * Helper to check if role is a researcher.
     */
    public boolean isResearcher() {
        return this == RESEARCHER;
    }

    /**
     * Get display name for UI.
     */
    public String getDisplayName() {
        return switch (this) {
            case FARMER -> "Farmer";
            case RESEARCHER -> "Researcher";
            case EXTENSION -> "Extension Officer";
            case ADMIN -> "Administrator";
            case GUEST -> "Guest";
        };
    }
}