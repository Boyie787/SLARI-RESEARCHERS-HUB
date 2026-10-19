package slari.researchers.controller;

import slari.researchers.dto.request.*;
import slari.researchers.dto.response.ApiResponse;
import slari.researchers.dto.response.PaginatedResponse;
import slari.researchers.dto.response.VarietyResponse;
import slari.researchers.enums.VarietyStatus;
import slari.researchers.service.VarietyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/varieties")
@RequiredArgsConstructor
@Slf4j
public class VarietyController {

    private final VarietyService varietyService;

    // ========== PUBLIC ENDPOINTS ==========

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<VarietyResponse>>> browse(
            @RequestParam(required = false) VarietyStatus status,
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<VarietyResponse> varieties = varietyService.browse(
                status != null ? status : VarietyStatus.PUBLISHED,
                crop, region, season, keyword, email, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Varieties retrieved",
                        PaginatedResponse.fromPage(varieties)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<VarietyResponse>>> featured() {
        return ResponseEntity.ok(
                ApiResponse.success("Featured varieties",
                        varietyService.getTopRated()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VarietyResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(
                ApiResponse.success("Variety retrieved",
                        varietyService.getById(id, email)));
    }

    @GetMapping("/researcher/{researcherId}")
    public ResponseEntity<ApiResponse<List<VarietyResponse>>> byResearcher(
            @PathVariable Long researcherId) {
        return ResponseEntity.ok(
                ApiResponse.success("Researcher varieties",
                        varietyService.getByResearcher(researcherId)));
    }

    // ========== ADMIN ONLY ==========

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<VarietyResponse>>> pending() {
        return ResponseEntity.ok(
                ApiResponse.success("Pending varieties",
                        varietyService.getPending()));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<VarietyResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) AdminActionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Variety approved and published",
                        varietyService.approve(id, request)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<VarietyResponse>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) AdminActionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Variety rejected",
                        varietyService.reject(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {
        varietyService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.success("Variety permanently deleted"));
    }

    // ========== RESEARCHER + ADMIN ==========

    @PostMapping
    public ResponseEntity<ApiResponse<VarietyResponse>> submit(
            @Valid @RequestBody VarietyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Variety submitted for admin review",
                        varietyService.submit(request, userDetails.getUsername())));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<VarietyResponse>>> submitBulk(
            @Valid @RequestBody BulkVarietyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Varieties submitted",
                        varietyService.submitBulk(request, userDetails.getUsername())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VarietyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody VarietyUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Variety updated",
                        varietyService.update(id, request, userDetails.getUsername())));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<VarietyResponse>> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Variety published",
                        varietyService.publish(id, userDetails.getUsername())));
    }

    @PatchMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<VarietyResponse>> unpublish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Variety unpublished",
                        varietyService.unpublish(id, userDetails.getUsername())));
    }

    @PostMapping(value = "/{id}/upload-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VarietyResponse>> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image format. Please upload a valid image.");
        }

        return ResponseEntity.ok(
                ApiResponse.success("Image uploaded successfully",
                        varietyService.uploadImage(id, image, userDetails.getUsername())));
    }

    @PostMapping(value = "/{id}/upload-document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VarietyResponse>> uploadDocument(
            @PathVariable Long id,
            @RequestParam("document") MultipartFile document,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (document.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }

        return ResponseEntity.ok(
                ApiResponse.success("Document uploaded successfully",
                        varietyService.uploadDocument(id, document, userDetails.getUsername())));
    }

    // ========== AUTHENTICATED USERS ==========

    @PostMapping("/{id}/rate")
    public ResponseEntity<ApiResponse<VarietyResponse>> rate(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Rating submitted",
                        varietyService.rate(id, userDetails.getUsername(), request)));
    }
}