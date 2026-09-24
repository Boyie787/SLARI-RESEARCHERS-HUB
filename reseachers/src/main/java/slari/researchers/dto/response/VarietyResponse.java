package slari.researchers.dto.response;

import slari.researchers.entity.Variety;
import slari.researchers.enums.VarietyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VarietyResponse {

    private Long id;
    private String name;
    private String crop;
    private String description;
    private String yieldAmount;
    private String yieldUnit;
    private String duration;
    private String resistance;
    private String region;
    private String season;
    private String soilType;
    private String rainfall;
    private String altitude;
    private VarietyStatus status;
    private String statusDisplay;  // Human-readable status
    private Integer year;
    private Integer views;
    private String institute;

    /* Computed / joined fields */
    private Long researcherId;
    private String researcherName;
    private Double averageRating;
    private Long ratingCount;
    private Long bookmarkCount;
    private Boolean isBookmarked;   // For current user
    private Integer userRating;     // Current user's rating
    private Boolean isOwner;        // If current user is the researcher

    private List<String> insights;
    private List<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String imagePath;
    private String documentPath;
    private String thumbnailPath;   // Compressed image for mobile

    /**
     * Convert Variety entity to VarietyResponse DTO.
     */
    public static VarietyResponse fromEntity(Variety v, long bookmarkCount) {
        return fromEntity(v, bookmarkCount, false, null, null);
    }

    /**
     * Convert Variety entity to VarietyResponse DTO with user-specific data.
     */
    public static VarietyResponse fromEntity(Variety v, long bookmarkCount,
                                             boolean isBookmarked,
                                             Integer userRating,
                                             String currentUserEmail) {
        String researcherName = v.getResearcher() != null ? v.getResearcher().getName() : null;
        Long researcherId = v.getResearcher() != null ? v.getResearcher().getId() : null;
        String researcherEmail = v.getResearcher() != null ? v.getResearcher().getEmail() : null;

        return VarietyResponse.builder()
                .id(v.getId())
                .name(v.getName())
                .crop(v.getCrop())
                .description(v.getDescription())
                .yieldAmount(v.getYieldAmount())
                .yieldUnit(v.getYieldUnit())
                .duration(v.getDuration())
                .resistance(v.getResistance())
                .region(v.getRegion())
                .season(v.getSeason())
                .soilType(v.getSoilType())
                .rainfall(v.getRainfall())
                .altitude(v.getAltitude())
                .status(v.getStatus())
                .statusDisplay(v.getStatus().getDisplayName())
                .year(v.getYear())
                .views(v.getViews())
                .institute(v.getInstitute())
                .researcherId(researcherId)
                .researcherName(researcherName)
                .averageRating(v.getAverageRating())
                .ratingCount(v.getRatingCount() != null ? v.getRatingCount() : 0L)
                .bookmarkCount(bookmarkCount)
                .isBookmarked(isBookmarked)
                .userRating(userRating)
                .isOwner(currentUserEmail != null && currentUserEmail.equals(researcherEmail))
                .insights(v.getInsights())
                .tags(v.getTags())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .imagePath(v.getImagePath())
                .documentPath(v.getDocumentPath())
                .thumbnailPath(v.getImagePath() != null ? 
                        v.getImagePath().replace("/uploads/images/", "/uploads/thumbnails/") : null)
                .build();
    }
}