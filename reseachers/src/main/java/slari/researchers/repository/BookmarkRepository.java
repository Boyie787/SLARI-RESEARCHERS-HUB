package slari.researchers.repository;

import slari.researchers.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndVarietyId(Long userId, Long varietyId);

    Optional<Bookmark> findByUserIdAndVarietyId(Long userId, Long varietyId);

    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Get bookmarks with pagination.
     */
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByVarietyId(Long varietyId);

    void deleteByUserIdAndVarietyId(Long userId, Long varietyId);

    /**
     * Count bookmarks by crop type (for analytics).
     */
    @Query("SELECT b.variety.crop, COUNT(b) FROM Bookmark b GROUP BY b.variety.crop")
    List<Object[]> countBookmarksByCrop();

    /**
     * Get most bookmarked varieties (for popularity ranking).
     */
    @Query("SELECT b.variety.id, COUNT(b) as bookmarkCount FROM Bookmark b " +
           "GROUP BY b.variety.id ORDER BY bookmarkCount DESC")
    List<Object[]> findMostBookmarkedVarieties(Pageable pageable);

    /**
     * Delete all bookmarks for a variety (cascade delete).
     */
    void deleteByVarietyId(Long varietyId);
}