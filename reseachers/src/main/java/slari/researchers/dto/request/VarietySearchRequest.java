package slari.researchers.dto.request;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class VarietySearchRequest {
    private String status;
    private String crop;
    private String region;
    private String season;
    private String keyword;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private Sort.Direction sortDirection = Sort.Direction.DESC;

    public Pageable toPageable() {
        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}