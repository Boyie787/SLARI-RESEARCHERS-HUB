package slari.researchers.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    private Long totalUsers;
    private Long totalFarmers;
    private Long totalResearchers;
    private Long totalExtensionOfficers;
    private Long totalVarieties;
    private Long publishedVarieties;
    private Long pendingVarieties;
    private Long draftVarieties;
    private Long rejectedVarieties;
    private Long totalRatings;
    private Double averageRating;
    private Long totalBookmarks;
    private Map<String, Long> varietiesByCrop;
    private Map<String, Long> usersByRegion;
    private Map<String, Long> ratingsByCrop;

    public static DashboardStatsResponse fromMap(Map<String, Long> stats) {
        return DashboardStatsResponse.builder()
                .totalUsers(stats.getOrDefault("totalUsers", 0L))
                .totalFarmers(stats.getOrDefault("farmers", 0L))
                .totalResearchers(stats.getOrDefault("researchers", 0L))
                .totalExtensionOfficers(stats.getOrDefault("extensionOfficers", 0L))
                .totalVarieties(stats.getOrDefault("totalVarieties", 0L))
                .publishedVarieties(stats.getOrDefault("publishedVarieties", 0L))
                .pendingVarieties(stats.getOrDefault("pendingVarieties", 0L))
                .draftVarieties(stats.getOrDefault("draftVarieties", 0L))
                .rejectedVarieties(stats.getOrDefault("rejectedVarieties", 0L))
                .build();
    }
}