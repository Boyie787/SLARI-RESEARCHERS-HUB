package slari.researchers.service;

import slari.researchers.dto.response.UserResponse;
import slari.researchers.dto.response.VarietyResponse;
import slari.researchers.entity.Bookmark;
import slari.researchers.entity.User;
import slari.researchers.entity.Variety;
import slari.researchers.enums.Role;
import slari.researchers.exception.ResourceNotFoundException;
import slari.researchers.repository.BookmarkRepository;
import slari.researchers.repository.UserRepository;
import slari.researchers.repository.VarietyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UserService — User profile management and bookmark operations
 *
 * Responsibilities:
 *   - Fetch current user's profile
 *   - List all users (admin only)
 *   - Activate / deactivate users (admin only)
 *   - Toggle variety bookmarks for farmers
 *   - Get a farmer's bookmarked varieties
 *   - Return platform statistics for admin dashboard
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository     userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final VarietyRepository  varietyRepository;

    /**
     * Get the logged-in user's own profile.
     * Called on page load to restore name, role, etc. from the server.
     */
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get all users — admin management panel.
     * @PreAuthorize ensures only ADMIN can call this.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with a specific role.
     * e.g. getAllFarmers() → admin sees all registered farmers
     *      extension officer needs farmer list to send recommendations
     */
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role)
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate a user account (soft delete).
     * Sets active = false. The user can no longer log in.
     * Does NOT delete any of their data.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        user.setActive(false);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    /**
     * Reactivate a previously deactivated user.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        user.setActive(true);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    /**
     * Toggle a bookmark — add if not bookmarked, remove if already bookmarked.
     *
     * Returns true  → variety was ADDED to bookmarks
     * Returns false → variety was REMOVED from bookmarks
     *
     * The unique constraint on (user_id, variety_id) in the DB
     * prevents duplicates even in concurrent requests.
     */
    @Transactional
    public boolean toggleBookmark(String userEmail, Long varietyId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Variety variety = varietyRepository.findById(varietyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variety not found with id: " + varietyId));

        if (bookmarkRepository.existsByUserIdAndVarietyId(user.getId(), varietyId)) {
            /* Already bookmarked — remove it */
            bookmarkRepository.deleteByUserIdAndVarietyId(user.getId(), varietyId);
            return false;
        } else {
            /* Not bookmarked — add it */
            bookmarkRepository.save(
                    Bookmark.builder()
                            .user(user)
                            .variety(variety)
                            .build()
            );
            return true;
        }
    }

    /**
     * Get all varieties a farmer has bookmarked.
     * Used on the My Bookmarks tab in reseach-list.html.
     */
    public List<VarietyResponse> getBookmarkedVarieties(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookmarkRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(bookmark -> VarietyResponse.fromEntity(
                        bookmark.getVariety(),
                        bookmarkRepository.countByVarietyId(bookmark.getVariety().getId())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get IDs of all varieties a user has bookmarked.
     * Used by the frontend to highlight which cards have the
     * bookmark icon filled in on page load.
     */
    public List<Long> getBookmarkedIds(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return bookmarkRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(b -> b.getVariety().getId())
                .collect(Collectors.toList());
    }

    /**
     * Platform-wide statistics for the Admin Stats tab.
     * Returns a map with labelled counts — easy to consume from any frontend.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> getPlatformStats() {
        return Map.of(
                "totalUsers",          userRepository.count(),
                "farmers",             userRepository.countByRole(Role.FARMER),
                "researchers",         userRepository.countByRole(Role.RESEARCHER),
                "extensionOfficers",   userRepository.countByRole(Role.EXTENSION),
                "totalVarieties",      varietyRepository.count(),
                "publishedVarieties",  varietyRepository.countByStatus(
                                           slari.researchers.enums.VarietyStatus.PUBLISHED),
                "pendingVarieties",    varietyRepository.countByStatus(
                                           slari.researchers.enums.VarietyStatus.PENDING)
        );
    }
}
