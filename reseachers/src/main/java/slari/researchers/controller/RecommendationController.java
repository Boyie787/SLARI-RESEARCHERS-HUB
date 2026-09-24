package slari.researchers.controller;

import slari.researchers.dto.request.RecommendationRequest;
import slari.researchers.dto.response.ApiResponse;
import slari.researchers.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RecommendationController — REST API for variety recommendations
 *
 * Base path: /api/recommendations
 *
 * Endpoints:
 *   POST /api/recommendations            → extension officer sends a recommendation
 *   GET  /api/recommendations/sent       → officer views their sent history
 *   GET  /api/recommendations/received   → farmer views recommendations received
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * POST /api/recommendations
     *
     * Extension officer sends a variety recommendation to a farmer.
     * Requires EXTENSION or ADMIN role (enforced in SecurityConfig + @PreAuthorize).
     *
     * Frontend call (Extension Dashboard recommend form):
     *   fetch('/api/recommendations', {
     *     method: 'POST',
     *     headers: {
     *       'Content-Type': 'application/json',
     *       'Authorization': `Bearer ${token}`
     *     },
     *     body: JSON.stringify({ farmerId: 3, varietyId: 1, note: '...' })
     *   })
     *
     * Returns: 201 Created + RecommendationResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RecommendationService.RecommendationResponse>> send(
            @Valid @RequestBody RecommendationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RecommendationService.RecommendationResponse rec =
                recommendationService.send(userDetails.getUsername(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recommendation sent successfully", rec));
    }

    /**
     * GET /api/recommendations/sent
     *
     * Extension officer views all recommendations they have sent.
     * Used on the Extension Dashboard → Sent tab.
     *
     * Frontend call:
     *   fetch('/api/recommendations/sent', {
     *     headers: { 'Authorization': `Bearer ${token}` }
     *   })
     */
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<RecommendationService.RecommendationResponse>>> sent(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Sent recommendations",
                        recommendationService.getSentByOfficer(userDetails.getUsername())));
    }

    /**
     * GET /api/recommendations/received
     *
     * Farmer views recommendations they have received from extension officers.
     * Future feature — farmers see what experts suggest for their area.
     *
     * Frontend call:
     *   fetch('/api/recommendations/received', {
     *     headers: { 'Authorization': `Bearer ${token}` }
     *   })
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<RecommendationService.RecommendationResponse>>> received(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Received recommendations",
                        recommendationService.getReceivedByFarmer(userDetails.getUsername())));
    }
}
