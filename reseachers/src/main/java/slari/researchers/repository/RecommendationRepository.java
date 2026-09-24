package slari.researchers.repository;

import slari.researchers.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByOfficerIdOrderByCreatedAtDesc(Long officerId);

    List<Recommendation> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    long countByOfficerId(Long officerId);

    /**
     * Get recommendations with pagination.
     */
    Page<Recommendation> findByOfficerIdOrderByCreatedAtDesc(Long officerId, Pageable pageable);

    Page<Recommendation> findByFarmerIdOrderByCreatedAtDesc(Long farmerId, Pageable pageable);

    /**
     * Count recommendations received by a farmer.
     */
    long countByFarmerId(Long farmerId);

    /**
     * Get recent recommendations for dashboard.
     */
    @Query("SELECT r FROM Recommendation r ORDER BY r.createdAt DESC")
    List<Recommendation> findRecentRecommendations(Pageable pageable);

    /**
     * Count recommendations by crop type.
     */
    @Query("SELECT r.variety.crop, COUNT(r) FROM Recommendation r GROUP BY r.variety.crop")
    List<Object[]> countRecommendationsByCrop();

    /**
     * Get recommendations sent in the last N days (native query).
     * Uses PostgreSQL INTERVAL syntax.
     */
    @Query(value = "SELECT * FROM recommendations r WHERE r.created_at > NOW() - INTERVAL :days DAY", nativeQuery = true)
    List<Recommendation> findRecommendationsInLastDays(@Param("days") int days);
}