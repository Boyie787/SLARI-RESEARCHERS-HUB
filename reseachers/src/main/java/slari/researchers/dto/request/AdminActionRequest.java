package slari.researchers.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminActionRequest {
    
    @Size(max = 500, message = "Review note must not exceed 500 characters")
    private String reviewNote;
    
    private Boolean notifyResearcher = true;
}