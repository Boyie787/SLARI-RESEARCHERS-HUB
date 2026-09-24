package slari.researchers.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkVarietyRequest {
    
    @NotEmpty(message = "At least one variety must be provided")
    @Size(max = 50, message = "Cannot upload more than 50 varieties at once")
    @Valid
    private List<VarietyRequest> varieties;
}