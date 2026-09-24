package slari.researchers.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Bookmark — JPA Entity mapped to the `bookmarks` table
 *
 * Represents a farmer saving / bookmarking a crop variety.
 * This is a join between a User (farmer) and a Variety.
 *
 * The composite unique constraint prevents a farmer from
 * bookmarking the same variety twice.
 *
 * PostgreSQL table:
 *   CREATE TABLE bookmarks (
 *     id          BIGSERIAL PRIMARY KEY,
 *     user_id     BIGINT NOT NULL REFERENCES users(id),
 *     variety_id  BIGINT NOT NULL REFERENCES varieties(id),
 *     created_at  TIMESTAMP,
 *     UNIQUE(user_id, variety_id)   -- one bookmark per farmer per variety
 *   );
 */
@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_bookmark_user_variety",
            columnNames = {"user_id", "variety_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The farmer who bookmarked the variety.
     * FetchType.LAZY — only load User data when explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The variety that was bookmarked.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variety_id", nullable = false)
    private Variety variety;

    /**
     * When the bookmark was created.
     * updatable=false — this never changes after creation.
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}