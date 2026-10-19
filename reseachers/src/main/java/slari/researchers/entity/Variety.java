package slari.researchers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import slari.researchers.enums.VarietyStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(
    name = "varieties",
    indexes = {
        @Index(name = "idx_variety_crop", columnList = "crop"),
        @Index(name = "idx_variety_region", columnList = "region"),
        @Index(name = "idx_variety_status", columnList = "status"),
        @Index(name = "idx_variety_researcher", columnList = "researcher_id"),
        @Index(name = "idx_variety_created", columnList = "created_at"),
        @Index(name = "idx_variety_yield", columnList = "yield_amount")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Variety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Variety name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Crop type is required")
    @Size(max = 50, message = "Crop must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String crop;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @PositiveOrZero(message = "Yield amount must be zero or positive")
    @Column(name = "yield_amount", length = 30)
    private String yieldAmount;

    @Size(max = 20, message = "Yield unit must not exceed 20 characters")
    @Column(name = "yield_unit", length = 20)
    @Builder.Default
    private String yieldUnit = "t/ha";

    @Pattern(regexp = "^[0-9]+\\s*[-]?\\s*[0-9]*\\s*(days|months|years)?$", 
             message = "Invalid duration format (e.g., '90 days', '3-4 months')")
    @Column(length = 50)
    private String duration;

    @Size(max = 200, message = "Resistance info must not exceed 200 characters")
    @Column(length = 200)
    private String resistance;

    @Size(max = 50, message = "Region must not exceed 50 characters")
    @Column(length = 50)
    private String region;

    @Size(max = 50, message = "Season must not exceed 50 characters")
    @Column(length = 50)
    private String season;

    @Size(max = 100, message = "Soil type must not exceed 100 characters")
    @Column(name = "soil_type", length = 100)
    private String soilType;

    @Size(max = 50, message = "Rainfall must not exceed 50 characters")
    @Column(length = 50)
    private String rainfall;

    @Size(max = 50, message = "Altitude must not exceed 50 characters")
    @Column(length = 50)
    private String altitude;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VarietyStatus status = VarietyStatus.DRAFT;

    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @PositiveOrZero(message = "Views must be zero or positive")
    @Column(nullable = false)
    @Builder.Default
    private Integer views = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id")
    private User researcher;

    @Size(max = 100, message = "Institute must not exceed 100 characters")
    @Column(length = 100)
    private String institute;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "variety_insights",
        joinColumns = @JoinColumn(name = "variety_id")
    )
    @Column(name = "insight", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> insights = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "variety_tags",
        joinColumns = @JoinColumn(name = "variety_id")
    )
    @Column(name = "tag", length = 60)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Formula("""
        (SELECT COALESCE(ROUND(AVG(r.score)::numeric, 1), 0)
         FROM ratings r
         WHERE r.variety_id = id)
    """)
    private Double averageRating;

    @Formula("""
        (SELECT COUNT(r.id)
         FROM ratings r
         WHERE r.variety_id = id)
    """)
    private Long ratingCount;

    @OneToMany(
        mappedBy = "variety",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(
        mappedBy = "variety",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "document_path", length = 500)
    private String documentPath;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private boolean deleted = false;

    // Helper methods
    public void incrementViews() {
        this.views = this.views == null ? 1 : this.views + 1;
    }

    public boolean isPublished() {
        return this.status == VarietyStatus.PUBLISHED;
    }

    public boolean isArchived() {
        return this.status == VarietyStatus.ARCHIVED;
    }

    public boolean hasImage() {
        return this.imagePath != null && !this.imagePath.isEmpty();
    }

    public boolean hasDocument() {
        return this.documentPath != null && !this.documentPath.isEmpty();
    }

    public boolean hasValidImageExtension() {
        if (imagePath == null) return true;
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        String lowerPath = imagePath.toLowerCase();
        return Arrays.stream(validExtensions)
            .anyMatch(lowerPath::endsWith);
    }

    public boolean hasValidDocumentExtension() {
        if (documentPath == null) return true;
        String[] validExtensions = {".pdf", ".docx", ".doc", ".txt"};
        String lowerPath = documentPath.toLowerCase();
        return Arrays.stream(validExtensions)
            .anyMatch(lowerPath::endsWith);
    }

    @PreRemove
    private void preRemove() {
        this.deleted = true;
    }
}