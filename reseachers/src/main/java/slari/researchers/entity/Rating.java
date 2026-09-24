package slari.researchers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Rating — JPA Entity mapped to the `ratings` table
 *
 * Stores a farmer's star rating (1–5) for a crop variety.
 *
 * Design decisions:
 * - One rating per user per variety (unique constraint).
 *   If a farmer rates again, we UPDATE their existing rating,
 *   we don't insert a second row.
 * - The average rating is NOT stored here. It's computed live
 *   on the Variety entity using @Formula to always be accurate.
 * - @Min(1) and @Max(5) validate at the application level.
 *   The DB check constraint adds a second layer of protection.
 *
 * PostgreSQL table:
 *   CREATE TABLE ratings (
 *     id          BIGSERIAL PRIMARY KEY,
 *     user_id     BIGINT NOT NULL REFERENCES users(id),
 *     variety_id  BIGINT NOT NULL REFERENCES varieties(id),
 *     score       INTEGER NOT NULL CHECK (score BETWEEN 1 AND 5),
 *     created_at  TIMESTAMP,
 *     updated_at  TIMESTAMP,
 *     UNIQUE(user_id, variety_id)
 *   );
 */
@Entity
@Table(
    name = "ratings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_rating_user_variety",
            columnNames = {"user_id", "variety_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who submitted the rating */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The variety being rated */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variety_id", nullable = false)
    private Variety variety;

    /**
     * Star score 1–5.
     * @Min and @Max trigger Bean Validation before saving.
     * columnDefinition adds the CHECK constraint in PostgreSQL.
     */
    @Min(1)
    @Max(5)
    @Column(nullable = false, columnDefinition = "integer check (score between 1 and 5)")
    private Integer score;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}