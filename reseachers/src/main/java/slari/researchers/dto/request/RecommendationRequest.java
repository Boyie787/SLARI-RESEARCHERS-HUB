package slari.researchers.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * RecommendationRequest — DTO for POST /api/recommendations
 *
 * Sent by an extension officer when recommending a variety to a farmer.
 *
 * Frontend sends:
 *   POST /api/recommendations
 *   {
 *     "farmerId":  3,
 *     "varietyId": 1,
 *     "note":      "This variety suits your Tonkolili farmland well..."
 *   }
 */
@Data
public class RecommendationRequest {

    @NotNull(message = "Farmer ID is required")
    private Long farmerId;

    @NotNull(message = "Variety ID is required")
    private Long varietyId;

    /** Optional personalised advice message */
    private String note;
}
