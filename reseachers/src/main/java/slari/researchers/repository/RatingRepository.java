package slari.researchers.repository;

import slari.researchers.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndVarietyId(Long userId, Long varietyId);

    boolean existsByUserIdAndVarietyId(Long userId, Long varietyId);

    @Query("SELECT ROUND(AVG(r.score), 1) FROM Rating r WHERE r.variety.id = :varietyId")
    Double getAverageRatingByVarietyId(@Param("varietyId") Long varietyId);

    long countByVarietyId(Long varietyId);

    /**
     * Get user's rating for a variety (if exists).
     */
    @Query("SELECT r.score FROM Rating r WHERE r.user.id = :userId AND r.variety.id = :varietyId")
    Optional<Integer> findScoreByUserIdAndVarietyId(@Param("userId") Long userId, @Param("varietyId") Long varietyId);

    /**
     * Get all ratings for a variety (for analytics).
     */
    @Query("SELECT r FROM Rating r WHERE r.variety.id = :varietyId ORDER BY r.createdAt DESC")
    List<Rating> findByVarietyId(@Param("varietyId") Long varietyId);

    /**
     * Get average rating for all varieties by a researcher.
     */
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.variety.researcher.id = :researcherId")
    Double getAverageRatingByResearcherId(@Param("researcherId") Long researcherId);

    /**
     * Count ratings per crop type (for analytics).
     */
    @Query("SELECT r.variety.crop, COUNT(r) FROM Rating r GROUP BY r.variety.crop")
    List<Object[]> countRatingsByCrop();

    /**
     * Delete all ratings for a variety (cascade delete).
     */
    void deleteByVarietyId(Long varietyId);
}