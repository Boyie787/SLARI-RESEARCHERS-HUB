package slari.researchers.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Recommendation — JPA Entity mapped to the `recommendations` table
 *
 * An extension officer sends a personalised variety recommendation
 * to a specific farmer. This links:
 *   - officer  (User with role EXTENSION)
 *   - farmer   (User with role FARMER)
 *   - variety  (a published Variety)
 *   - note     (personalised advice text)
 *
 * Design note:
 * Both `officer` and `farmer` reference the same `users` table,
 * so we need two separate @JoinColumn definitions with different
 * foreign key names to avoid column name collision.
 *
 * PostgreSQL table:
 *   CREATE TABLE recommendations (
 *     id          BIGSERIAL PRIMARY KEY,
 *     officer_id  BIGINT NOT NULL REFERENCES users(id),
 *     farmer_id   BIGINT NOT NULL REFERENCES users(id),
 *     variety_id  BIGINT NOT NULL REFERENCES varieties(id),
 *     note        TEXT,
 *     created_at  TIMESTAMP
 *   );
 */
@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The extension officer sending the recommendation.
     * name = "officer_id" gives this FK column a clear name.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    /**
     * The farmer receiving the recommendation.
     * Different name = "farmer_id" to avoid conflict with officer_id.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private User farmer;

    /**
     * The variety being recommended.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variety_id", nullable = false)
    private Variety variety;

    /**
     * Personalised message explaining why this variety suits
     * this farmer's specific land, climate, and goals.
     * TEXT allows unlimited length.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}