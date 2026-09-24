package slari.researchers.dto.response;

import slari.researchers.entity.Recommendation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecommendationResponse {
    private Long id;
    private Long officerId;
    private String officerName;
    private String officerEmail;
    private Long farmerId;
    private String farmerName;
    private String farmerLocation;
    private Long varietyId;
    private String varietyName;
    private String varietyCrop;
    private String note;
    private LocalDateTime createdAt;
    private String createdAtDisplay;

    public static RecommendationResponse fromEntity(Recommendation rec) {
        return RecommendationResponse.builder()
                .id(rec.getId())
                .officerId(rec.getOfficer().getId())
                .officerName(rec.getOfficer().getName())
                .officerEmail(rec.getOfficer().getEmail())
                .farmerId(rec.getFarmer().getId())
                .farmerName(rec.getFarmer().getName())
                .farmerLocation(rec.getFarmer().getLocation())
                .varietyId(rec.getVariety().getId())
                .varietyName(rec.getVariety().getName())
                .varietyCrop(rec.getVariety().getCrop())
                .note(rec.getNote())
                .createdAt(rec.getCreatedAt())
                .createdAtDisplay(rec.getCreatedAt() != null ? 
                        rec.getCreatedAt().toString() : null)
                .build();
    }
}