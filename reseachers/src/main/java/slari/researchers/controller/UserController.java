package slari.researchers.controller;

import slari.researchers.dto.response.ApiResponse;
import slari.researchers.dto.response.UserResponse;
import slari.researchers.dto.response.VarietyResponse;
import slari.researchers.enums.Role;
import slari.researchers.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController — REST API for user profile, bookmarks, and role-based user lists
 *
 * Base path: /api/users
 *
 * Endpoints:
 *   GET    /api/users/me                        → logged-in user profile
 *   GET    /api/users/me/bookmarks              → bookmarked varieties
 *   GET    /api/users/me/bookmarked-ids         → just the IDs (for UI state)
 *   POST   /api/users/me/bookmarks/{id}         → toggle bookmark
 *   GET    /api/users/by-role?role=FARMER       → users by any role
 *   GET    /api/users/farmers                   → all farmers (shortcut)
 *   GET    /api/users/researchers               → all researchers
 *   GET    /api/users/extension-officers        → all extension officers
 *   GET    /api/users                           → ALL users (admin only)
 *   GET    /api/users/stats                     → platform stats (admin only)
 *   PATCH  /api/users/{id}/deactivate           → deactivate user (admin)
 *   PATCH  /api/users/{id}/activate             → reactivate user (admin)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /* ─────────────────────────────────────────
       CURRENT USER PROFILE
    ───────────────────────────────────────── */

    /**
     * GET /api/users/me
     * Returns the logged-in user's own profile.
     * Called on every page load to restore name, role, institute from the server.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved",
                        userService.getProfile(userDetails.getUsername())));
    }

    /**
     * GET /api/users/me/bookmarks
     * Full variety objects the logged-in farmer has bookmarked.
     * Used on the My Bookmarks tab.
     */
    @GetMapping("/me/bookmarks")
    public ResponseEntity<ApiResponse<List<VarietyResponse>>> getMyBookmarks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Bookmarks retrieved",
                        userService.getBookmarkedVarieties(userDetails.getUsername())));
    }

    /**
     * GET /api/users/me/bookmarked-ids
     * Returns only the IDs of bookmarked varieties.
     * Frontend uses this on page load to pre-fill the bookmark icons
     * on variety cards without loading full variety data.
     */
    @GetMapping("/me/bookmarked-ids")
    public ResponseEntity<ApiResponse<List<Long>>> getBookmarkedIds(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Bookmarked IDs",
                        userService.getBookmarkedIds(userDetails.getUsername())));
    }

    /**
     * POST /api/users/me/bookmarks/{varietyId}
     * Toggle a bookmark on or off.
     *   First call  → adds bookmark, returns true
     *   Second call → removes it,   returns false
     */
    @PostMapping("/me/bookmarks/{varietyId}")
    public ResponseEntity<ApiResponse<Boolean>> toggleBookmark(
            @PathVariable Long varietyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean added = userService.toggleBookmark(
                userDetails.getUsername(), varietyId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        added ? "Variety bookmarked" : "Bookmark removed", added));
    }

    /* ─────────────────────────────────────────
       USERS BY ROLE
       All four roles now have their own endpoint
       PLUS a generic ?role= parameter endpoint
    ───────────────────────────────────────── */

    /**
     * GET /api/users/by-role?role=FARMER
     * GET /api/users/by-role?role=RESEARCHER
     * GET /api/users/by-role?role=EXTENSION
     * GET /api/users/by-role?role=ADMIN
     *
     * Generic endpoint — returns active users of ANY specified role.
     * The role parameter must match the Role enum exactly (uppercase).
     *
     * Used by:
     *   - Extension officer recommendation form (role=FARMER)
     *   - Admin user management panel (all roles)
     *   - Any future feature needing a filtered user list
     */
    @GetMapping("/by-role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @RequestParam Role role) {
        return ResponseEntity.ok(
                ApiResponse.success(role.name() + " users retrieved",
                        userService.getUsersByRole(role)));
    }

    /**
     * GET /api/users/farmers
     * All active FARMER accounts.
     * Used by extension officers to populate the farmer dropdown
     * in the recommendation form.
     */
    @GetMapping("/farmers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllFarmers() {
        return ResponseEntity.ok(
                ApiResponse.success("Farmers retrieved",
                        userService.getUsersByRole(Role.FARMER)));
    }

    /**
     * GET /api/users/researchers
     * All active RESEARCHER accounts.
     * Used by admin to list all scientists on the platform.
     */
    @GetMapping("/researchers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllResearchers() {
        return ResponseEntity.ok(
                ApiResponse.success("Researchers retrieved",
                        userService.getUsersByRole(Role.RESEARCHER)));
    }

    /**
     * GET /api/users/extension-officers
     * All active EXTENSION officer accounts.
     * Used by admin to manage extension officers.
     */
    @GetMapping("/extension-officers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllExtensionOfficers() {
        return ResponseEntity.ok(
                ApiResponse.success("Extension officers retrieved",
                        userService.getUsersByRole(Role.EXTENSION)));
    }

    /* ─────────────────────────────────────────
       ADMIN ENDPOINTS
    ───────────────────────────────────────── */

    /**
     * GET /api/users
     * Admin only — returns every user across all roles.
     * Role enforcement is done in UserService via @PreAuthorize.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("All users retrieved",
                        userService.getAllUsers()));
    }

    /**
     * GET /api/users/stats
     * Admin only — platform-wide counts per role and variety status.
     * Drives the Admin Stats dashboard tab.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(
                ApiResponse.success("Platform stats",
                        userService.getPlatformStats()));
    }

    /**
     * PATCH /api/users/{id}/deactivate
     * Admin only — soft-deletes a user (active = false).
     * The user cannot log in but their data is preserved.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated",
                        userService.deactivateUser(id)));
    }

    /**
     * PATCH /api/users/{id}/activate
     * Admin only — restores a previously deactivated account.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User activated",
                        userService.activateUser(id)));
    }
}
