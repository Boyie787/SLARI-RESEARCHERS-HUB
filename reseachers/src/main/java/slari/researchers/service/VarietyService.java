package slari.researchers.service;

import slari.researchers.dto.request.AdminActionRequest;
import slari.researchers.dto.request.BulkVarietyRequest;
import slari.researchers.dto.request.RatingRequest;
import slari.researchers.dto.request.VarietyRequest;
import slari.researchers.dto.request.VarietyUpdateRequest;
import slari.researchers.dto.response.VarietyResponse;
import slari.researchers.entity.Rating;
import slari.researchers.entity.User;
import slari.researchers.entity.Variety;
import slari.researchers.enums.Role;
import slari.researchers.enums.VarietyStatus;
import slari.researchers.exception.ResourceNotFoundException;
import slari.researchers.repository.BookmarkRepository;
import slari.researchers.repository.RatingRepository;
import slari.researchers.repository.UserRepository;
import slari.researchers.repository.VarietyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VarietyService {

    private final VarietyRepository varietyRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final BookmarkRepository bookmarkRepository;
    private final FileStorageService fileStorageService;

    /* ══════════════════════════════════════
       BROWSE & READ
       ══════════════════════════════════════ */

    /**
     * Browse varieties with optional filters (original method - keeps compatibility).
     */
    public List<VarietyResponse> browse(
        VarietyStatus status,
        String crop,
        String region,
        String season,
        String keyword) {

    String statusStr = status != null ? status.name() : null;

    List<Variety> varieties = varietyRepository.findWithFilters(
            statusStr, crop, region, season,
            (keyword != null && keyword.isBlank()) ? null : keyword
    );

    return varieties.stream()
            .map(v -> {
                Double avgRating = ratingRepository
                        .getAverageRatingByVarietyId(v.getId());
                Long ratingCnt = ratingRepository
                        .countByVarietyId(v.getId());
                v.setAverageRating(avgRating);
                v.setRatingCount(ratingCnt);
                return VarietyResponse.fromEntity(v,
                        bookmarkRepository.countByVarietyId(v.getId()));
            })
            .collect(Collectors.toList());
}

    /**
     * Browse varieties with pagination and user context.
     * Used by the controller for paginated responses.
     */
    public Page<VarietyResponse> browse(
            VarietyStatus status,
            String crop,
            String region,
            String season,
            String keyword,
            String userEmail,
            Pageable pageable) {

        String statusStr = status != null ? status.name() : null;

        Page<Variety> varieties = varietyRepository.findWithFilters(
                statusStr,
                crop,
                region,
                season,
                (keyword != null && keyword.isBlank()) ? null : keyword,
                pageable
        );

        return varieties.map(v -> {
            // Get user-specific data if logged in
            boolean isBookmarked = false;
            Integer userRating = null;
            if (userEmail != null) {
                User user = userRepository.findByEmail(userEmail).orElse(null);
                if (user != null) {
                    isBookmarked = bookmarkRepository.existsByUserIdAndVarietyId(
                            user.getId(), v.getId());
                    userRating = ratingRepository.findByUserIdAndVarietyId(
                                    user.getId(), v.getId())
                            .map(Rating::getScore)
                            .orElse(null);
                }
            }
            return VarietyResponse.fromEntity(v,
                    bookmarkRepository.countByVarietyId(v.getId()),
                    isBookmarked, userRating, userEmail);
        });
    }

    /**
     * Get single variety detail by ID.
     * Increments the view counter on every call.
     */
    @Transactional
    public VarietyResponse getById(Long id) {
        Variety variety = findOrThrow(id);
        variety.incrementViews();
        varietyRepository.save(variety);
        return toResponse(variety);
    }

    /**
     * Get variety by ID with user context.
     * Used by the controller to provide user-specific data.
     */
    @Transactional
    public VarietyResponse getById(Long id, String userEmail) {
        Variety variety = findOrThrow(id);
        variety.incrementViews();
        varietyRepository.save(variety);

        // Get user-specific data if logged in
        boolean isBookmarked = false;
        Integer userRating = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                isBookmarked = bookmarkRepository.existsByUserIdAndVarietyId(
                        user.getId(), id);
                userRating = ratingRepository.findByUserIdAndVarietyId(
                                user.getId(), id)
                        .map(Rating::getScore)
                        .orElse(null);
            }
        }

        return VarietyResponse.fromEntity(variety,
                bookmarkRepository.countByVarietyId(id),
                isBookmarked, userRating, userEmail);
    }

    /**
     * Get top-rated published varieties for featured section.
     */
    public List<VarietyResponse> getTopRated() {
        return varietyRepository.findTopRatedPublished()
                .stream()
                .map(v -> VarietyResponse.fromEntity(v,
                        bookmarkRepository.countByVarietyId(v.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get varieties by researcher ID.
     */
    public List<VarietyResponse> getByResearcher(Long researcherId) {
        return varietyRepository
                .findByResearcherIdOrderByCreatedAtDesc(researcherId)
                .stream()
                .map(v -> VarietyResponse.fromEntity(v,
                        bookmarkRepository.countByVarietyId(v.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Get pending varieties for admin approval.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<VarietyResponse> getPending() {
        return varietyRepository
                .findByStatusOrderByCreatedAtAsc(VarietyStatus.PENDING)
                .stream()
                .map(v -> VarietyResponse.fromEntity(v,
                        bookmarkRepository.countByVarietyId(v.getId())))
                .collect(Collectors.toList());
    }

    /* ══════════════════════════════════════
       SUBMIT (new variety)
       ══════════════════════════════════════ */

    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public VarietyResponse submit(VarietyRequest request, String userEmail) {
        // Validate required fields
        validateVarietyRequest(request);

        User researcher = findUserOrThrow(userEmail);

        Variety variety = Variety.builder()
                .name(request.getName().trim())
                .crop(request.getCrop().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .yieldAmount(request.getYieldAmount())
                .yieldUnit(request.getYieldUnit() != null ? request.getYieldUnit() : "t/ha")
                .duration(request.getDuration())
                .resistance(request.getResistance())
                .region(request.getRegion())
                .season(request.getSeason())
                .soilType(request.getSoilType())
                .rainfall(request.getRainfall())
                .altitude(request.getAltitude())
                .institute(request.getInstitute() != null ? request.getInstitute() : researcher.getInstitute())
                .year(request.getYear())
                .researcher(researcher)
                .status(VarietyStatus.PENDING)
                .insights(request.getInsights() != null ? request.getInsights() : List.of())
                .tags(request.getTags() != null ? request.getTags() : List.of())
                .build();

        Variety saved = varietyRepository.save(variety);
        log.info("New variety submitted: {} by researcher: {}", saved.getName(), researcher.getEmail());

        return toResponse(saved);
    }

    /**
     * Submit multiple varieties at once (bulk upload).
     */
    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public List<VarietyResponse> submitBulk(BulkVarietyRequest request, String userEmail) {
        User researcher = findUserOrThrow(userEmail);

        return request.getVarieties().stream()
                .map(varietyRequest -> {
                    Variety variety = Variety.builder()
                            .name(varietyRequest.getName().trim())
                            .crop(varietyRequest.getCrop().trim())
                            .description(varietyRequest.getDescription() != null ?
                                    varietyRequest.getDescription().trim() : null)
                            .yieldAmount(varietyRequest.getYieldAmount())
                            .yieldUnit(varietyRequest.getYieldUnit() != null ?
                                    varietyRequest.getYieldUnit() : "t/ha")
                            .duration(varietyRequest.getDuration())
                            .resistance(varietyRequest.getResistance())
                            .region(varietyRequest.getRegion())
                            .season(varietyRequest.getSeason())
                            .soilType(varietyRequest.getSoilType())
                            .rainfall(varietyRequest.getRainfall())
                            .altitude(varietyRequest.getAltitude())
                            .institute(varietyRequest.getInstitute() != null ?
                                    varietyRequest.getInstitute() : researcher.getInstitute())
                            .year(varietyRequest.getYear())
                            .researcher(researcher)
                            .status(VarietyStatus.PENDING)
                            .insights(varietyRequest.getInsights() != null ?
                                    varietyRequest.getInsights() : List.of())
                            .tags(varietyRequest.getTags() != null ?
                                    varietyRequest.getTags() : List.of())
                            .build();

                    Variety saved = varietyRepository.save(variety);
                    log.info("Bulk variety submitted: {} by researcher: {}",
                            saved.getName(), researcher.getEmail());
                    return toResponse(saved);
                })
                .collect(Collectors.toList());
    }

    private void validateVarietyRequest(VarietyRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Variety name is required");
        }
        if (request.getCrop() == null || request.getCrop().isBlank()) {
            throw new IllegalArgumentException("Crop type is required");
        }
        if (request.getYieldAmount() == null || request.getYieldAmount().isBlank()) {
            throw new IllegalArgumentException("Yield amount is required");
        }
    }

    /* ══════════════════════════════════════
       UPDATE (edit existing variety)
       ══════════════════════════════════════ */

    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public VarietyResponse update(Long id, VarietyUpdateRequest request, String userEmail) {
        Variety variety = findOrThrow(id);
        User user = findUserOrThrow(userEmail);

        /* Ownership check — researcher can only edit their own varieties */
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = variety.getResearcher() != null &&
                variety.getResearcher().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException(
                    "You can only edit varieties that you submitted.");
        }

        /* Null-safe merge — only update fields that were provided */
        if (request.getName() != null) variety.setName(request.getName());
        if (request.getCrop() != null) variety.setCrop(request.getCrop());
        if (request.getDescription() != null) variety.setDescription(request.getDescription());
        if (request.getYieldAmount() != null) variety.setYieldAmount(request.getYieldAmount());
        if (request.getYieldUnit() != null) variety.setYieldUnit(request.getYieldUnit());
        if (request.getDuration() != null) variety.setDuration(request.getDuration());
        if (request.getResistance() != null) variety.setResistance(request.getResistance());
        if (request.getRegion() != null) variety.setRegion(request.getRegion());
        if (request.getSeason() != null) variety.setSeason(request.getSeason());
        if (request.getSoilType() != null) variety.setSoilType(request.getSoilType());
        if (request.getRainfall() != null) variety.setRainfall(request.getRainfall());
        if (request.getAltitude() != null) variety.setAltitude(request.getAltitude());
        if (request.getInstitute() != null) variety.setInstitute(request.getInstitute());
        if (request.getYear() != null) variety.setYear(request.getYear());
        if (request.getInsights() != null) variety.setInsights(request.getInsights());
        if (request.getTags() != null) variety.setTags(request.getTags());

        /* If was PUBLISHED — reset to PENDING for re-approval */
        if (variety.getStatus() == VarietyStatus.PUBLISHED) {
            variety.setStatus(VarietyStatus.PENDING);
        }

        Variety saved = varietyRepository.save(variety);
        log.info("Variety updated: {} by user: {}", saved.getName(), userEmail);
        return toResponse(saved);
    }

    /* ══════════════════════════════════════
       ADMIN ACTIONS
       ══════════════════════════════════════ */

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VarietyResponse approve(Long id) {
        Variety variety = findOrThrow(id);
        variety.setStatus(VarietyStatus.PUBLISHED);
        Variety saved = varietyRepository.save(variety);
        log.info("Variety approved and published: {}", saved.getName());
        return toResponse(saved);
    }

    /**
     * Admin approves a pending variety with admin note.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VarietyResponse approve(Long id, AdminActionRequest request) {
        Variety variety = findOrThrow(id);
        variety.setStatus(VarietyStatus.PUBLISHED);
        Variety saved = varietyRepository.save(variety);
        log.info("Variety approved and published: {} (note: {})",
                saved.getName(), request != null ? request.getReviewNote() : "No note");
        // TODO: Send notification to researcher if request.isNotifyResearcher()
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VarietyResponse reject(Long id) {
        Variety variety = findOrThrow(id);
        variety.setStatus(VarietyStatus.DRAFT);
        Variety saved = varietyRepository.save(variety);
        log.info("Variety rejected and returned to draft: {}", saved.getName());
        return toResponse(saved);
    }

    /**
     * Admin rejects a pending variety with admin note.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VarietyResponse reject(Long id, AdminActionRequest request) {
        Variety variety = findOrThrow(id);
        variety.setStatus(VarietyStatus.DRAFT);
        Variety saved = varietyRepository.save(variety);
        log.info("Variety rejected and returned to draft: {} (reason: {})",
                saved.getName(), request != null ? request.getReviewNote() : "No reason provided");
        // TODO: Send notification to researcher with reason
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Variety variety = findOrThrow(id);

        /* Delete associated files from disk */
        fileStorageService.deleteFile(variety.getImagePath());
        fileStorageService.deleteFile(variety.getDocumentPath());

        varietyRepository.delete(variety);
        log.info("Variety permanently deleted: {}", variety.getName());
    }

    /* ══════════════════════════════════════
       RESEARCHER PUBLISH / UNPUBLISH
       ══════════════════════════════════════ */

    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public VarietyResponse publish(Long id, String userEmail) {
        Variety variety = findOrThrow(id);
        User user = findUserOrThrow(userEmail);

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = variety.getResearcher() != null &&
                variety.getResearcher().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You can only publish your own varieties.");
        }

        if (variety.getStatus() == VarietyStatus.PENDING) {
            throw new RuntimeException(
                    "This variety is awaiting admin approval and cannot be self-published.");
        }

        variety.setStatus(VarietyStatus.PUBLISHED);
        Variety saved = varietyRepository.save(variety);
        log.info("Variety published by researcher: {}", saved.getName());
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public VarietyResponse unpublish(Long id, String userEmail) {
        Variety variety = findOrThrow(id);
        User user = findUserOrThrow(userEmail);

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = variety.getResearcher() != null &&
                variety.getResearcher().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You can only unpublish your own varieties.");
        }

        variety.setStatus(VarietyStatus.DRAFT);
        Variety saved = varietyRepository.save(variety);
        log.info("Variety unpublished by researcher: {}", saved.getName());
        return toResponse(saved);
    }

    /* ══════════════════════════════════════
       FILE UPLOAD
       ══════════════════════════════════════ */

    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public VarietyResponse uploadImage(Long id, MultipartFile image, String userEmail) {
        Variety variety = findOrThrow(id);
        checkOwnership(variety, findUserOrThrow(userEmail));

        // Delete old image
        if (variety.getImagePath() != null) {
            fileStorageService.deleteFile(variety.getImagePath());
        }

        String imagePath = fileStorageService.storeImage(image);
        variety.setImagePath(imagePath);

        Variety saved = varietyRepository.save(variety);
        log.info("Image uploaded for variety: {}", saved.getName());

        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('RESEARCHER','ADMIN')")
    public VarietyResponse uploadDocument(Long id, MultipartFile document, String userEmail) {
        Variety variety = findOrThrow(id);
        checkOwnership(variety, findUserOrThrow(userEmail));

        /* Delete old document before saving new one */
        if (variety.getDocumentPath() != null) {
            fileStorageService.deleteFile(variety.getDocumentPath());
        }

        String documentPath = fileStorageService.storeDocument(document);
        variety.setDocumentPath(documentPath);

        Variety saved = varietyRepository.save(variety);
        log.info("Document uploaded for variety: {}", saved.getName());

        return toResponse(saved);
    }

    /* ══════════════════════════════════════
       RATING
       ══════════════════════════════════════ */

    @Transactional
    public VarietyResponse rate(Long varietyId, String userEmail, RatingRequest request) {
        Variety variety = findOrThrow(varietyId);
        User user = findUserOrThrow(userEmail);

        // Only farmers can rate
        if (user.getRole() != Role.FARMER) {
            throw new RuntimeException("Only farmers can rate varieties");
        }

        // Only published varieties can be rated
        if (variety.getStatus() != VarietyStatus.PUBLISHED) {
            throw new RuntimeException("Only published varieties can be rated");
        }

        // Validate score
        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Rating rating = ratingRepository
                .findByUserIdAndVarietyId(user.getId(), varietyId)
                .orElse(Rating.builder()
                        .user(user)
                        .variety(variety)
                        .build());

        rating.setScore(request.getScore());
        ratingRepository.save(rating);

        log.info("User {} rated variety {} with score: {}", user.getEmail(), varietyId, request.getScore());

        return toResponse(varietyRepository.findById(varietyId).orElseThrow());
    }

    /* ══════════════════════════════════════
       PRIVATE HELPERS
       ══════════════════════════════════════ */

    /**
     * Find variety by ID or throw 404
     */
    private Variety findOrThrow(Long id) {
        return varietyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Variety not found with id: " + id));
    }

    /**
     * Find user by email or throw 404
     */
    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    /**
     * Convert Variety entity to VarietyResponse DTO
     */
    private VarietyResponse toResponse(Variety v) {
        return VarietyResponse.fromEntity(v,
                bookmarkRepository.countByVarietyId(v.getId()));
    }

    /**
     * Ownership check — researcher can only modify their own varieties.
     * Admin can modify any variety.
     */
    private void checkOwnership(Variety variety, User user) {
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = variety.getResearcher() != null &&
                variety.getResearcher().getId().equals(user.getId());
        if (!isAdmin && !isOwner) {
            throw new RuntimeException(
                    "You can only modify varieties that you submitted.");
        }
    }
}