package slari.researchers.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * RatingRequest — DTO for POST /api/varieties/{id}/rate
 *
 * A farmer submits a star rating for a variety.
 *
 * Frontend sends:
 *   POST /api/varieties/1/rate
 *   { "score": 5 }
 */
@Data
public class RatingRequest {

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer score;
}
