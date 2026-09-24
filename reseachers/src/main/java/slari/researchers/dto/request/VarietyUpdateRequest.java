package slari.researchers.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VarietyUpdateRequest {

    @NotBlank(message = "Variety name is required")
    @Size(min = 2, max = 100, message = "Variety name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Crop type is required")
    @Size(max = 50, message = "Crop must not exceed 50 characters")
    private String crop;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Pattern(regexp = "^[0-9]+(\\.[0-9]+)?$", message = "Yield must be a valid number")
    private String yieldAmount;

    @Size(max = 20, message = "Yield unit must not exceed 20 characters")
    private String yieldUnit;

    @Pattern(
        regexp = "^[0-9]+\\s*[-]?\\s*[0-9]*\\s*(days|months|years)$",
        message = "Duration must be in format: '90 days', '3-4 months', etc."
    )
    private String duration;

    @Size(max = 200, message = "Resistance info must not exceed 200 characters")
    private String resistance;

    @Size(max = 50, message = "Region must not exceed 50 characters")
    private String region;

    @Size(max = 50, message = "Season must not exceed 50 characters")
    private String season;

    @Size(max = 100, message = "Soil type must not exceed 100 characters")
    private String soilType;

    @Size(max = 50, message = "Rainfall must not exceed 50 characters")
    private String rainfall;

    @Size(max = 50, message = "Altitude must not exceed 50 characters")
    private String altitude;

    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @Size(max = 100, message = "Institute must not exceed 100 characters")
    private String institute;

    @Size(max = 10, message = "Cannot have more than 10 insights")
    private List<String> insights = new ArrayList<>();

    @Size(max = 10, message = "Cannot have more than 10 tags")
    private List<String> tags = new ArrayList<>();
}