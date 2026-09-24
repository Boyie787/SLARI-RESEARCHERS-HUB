package slari.researchers.service;

import slari.researchers.dto.request.RecommendationRequest;
import slari.researchers.dto.response.ApiResponse;
import slari.researchers.entity.Recommendation;
import slari.researchers.entity.User;
import slari.researchers.entity.Variety;
import slari.researchers.exception.ResourceNotFoundException;
import slari.researchers.repository.RecommendationRepository;
import slari.researchers.repository.UserRepository;
import slari.researchers.repository.VarietyRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RecommendationService — Business logic for variety recommendations
 *
 * Extension officers use this to send personalised variety
 * recommendations to specific farmers in their coverage area.
 *
 * Operations:
 *   send()          — extension officer sends a recommendation
 *   getSent()       — officer views their sent history
 *   getReceived()   — farmer views recommendations they received
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository           userRepository;
    private final VarietyRepository        varietyRepository;

    /**
     * Inner DTO for returning recommendation data.
     * Avoids exposing the full entity with lazy-loaded proxies.
     */
    @Data
    @Builder
    public static class RecommendationResponse {
        private Long          id;
        private Long          officerId;
        private String        officerName;
        private Long          farmerId;
        private String        farmerName;
        private String        farmerLocation;
        private Long          varietyId;
        private String        varietyName;
        private String        varietyCrop;
        private String        note;
        private LocalDateTime createdAt;
    }

    /**
     * Send a variety recommendation from an extension officer to a farmer.
     *
     * @PreAuthorize ensures only EXTENSION and ADMIN roles can call this.
     * A farmer cannot recommend varieties to other farmers.
     *
     * @param officerEmail logged-in extension officer's email
     * @param request      farmerId + varietyId + optional note
     */
    @Transactional
    @PreAuthorize("hasAnyRole('EXTENSION','ADMIN')")
    public RecommendationResponse send(String officerEmail, RecommendationRequest request) {

        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        User farmer = userRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Farmer not found with id: " + request.getFarmerId()));

        Variety variety = varietyRepository.findById(request.getVarietyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variety not found with id: " + request.getVarietyId()));

        Recommendation recommendation = Recommendation.builder()
                .officer(officer)
                .farmer(farmer)
                .variety(variety)
                .note(request.getNote())
                .build();

        Recommendation saved = recommendationRepository.save(recommendation);
        return toResponse(saved);
    }

    /**
     * Get all recommendations sent by a specific extension officer.
     * Used on the Sent tab of the Extension Dashboard.
     */
    @PreAuthorize("hasAnyRole('EXTENSION','ADMIN')")
    public List<RecommendationResponse> getSentByOfficer(String officerEmail) {
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        return recommendationRepository
                .findByOfficerIdOrderByCreatedAtDesc(officer.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all recommendations received by a specific farmer.
     * Farmers can see what varieties have been suggested for them.
     */
    public List<RecommendationResponse> getReceivedByFarmer(String farmerEmail) {
        User farmer = userRepository.findByEmail(farmerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        return recommendationRepository
                .findByFarmerIdOrderByCreatedAtDesc(farmer.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Recommendation entity to RecommendationResponse DTO.
     * Private helper used by all methods above.
     */
    private RecommendationResponse toResponse(Recommendation rec) {
        return RecommendationResponse.builder()
                .id(rec.getId())
                .officerId(rec.getOfficer().getId())
                .officerName(rec.getOfficer().getName())
                .farmerId(rec.getFarmer().getId())
                .farmerName(rec.getFarmer().getName())
                .farmerLocation(rec.getFarmer().getLocation())
                .varietyId(rec.getVariety().getId())
                .varietyName(rec.getVariety().getName())
                .varietyCrop(rec.getVariety().getCrop())
                .note(rec.getNote())
                .createdAt(rec.getCreatedAt())
                .build();
    }
}
