package slari.researchers.repository;

import slari.researchers.entity.Variety;
import slari.researchers.enums.VarietyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VarietyRepository extends JpaRepository<Variety, Long> {

    /**
     * Master filter query without pagination.
     *
     * Important:
     * We return only v.* here. Rating calculations are handled
     * separately by RatingRepository instead of mixing calculated
     * columns with the Variety entity mapping.
     */
    @Query(value = """
        SELECT DISTINCT v.*
        FROM varieties v
        WHERE v.deleted = false
        AND (
            COALESCE(CAST(:status AS text), '') = ''
            OR v.status = CAST(:status AS text)
        )
        AND (
            COALESCE(CAST(:crop AS text), '') = ''
            OR v.crop ILIKE CONCAT('%', CAST(:crop AS text), '%')
        )
        AND (
            COALESCE(CAST(:region AS text), '') = ''
            OR v.region ILIKE CONCAT('%', CAST(:region AS text), '%')
            OR LOWER(v.region) = 'all zones'
        )
        AND (
            COALESCE(CAST(:season AS text), '') = ''
            OR v.season ILIKE CONCAT('%', CAST(:season AS text), '%')
            OR LOWER(v.season) = 'bimodal'
        )
        AND (
            COALESCE(CAST(:keyword AS text), '') = ''
            OR v.name ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            OR v.crop ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            OR v.description ILIKE CONCAT('%', CAST(:keyword AS text), '%')
        )
        ORDER BY v.created_at DESC
        """,
        nativeQuery = true)
    List<Variety> findWithFilters(
            @Param("status") String status,
            @Param("crop") String crop,
            @Param("region") String region,
            @Param("season") String season,
            @Param("keyword") String keyword
    );


    /**
     * Master filter query with pagination.
     *
     * Only actual columns from the varieties table are selected.
     * Rating information is retrieved separately.
     */
    @Query(value = """
        SELECT DISTINCT v.*
        FROM varieties v
        WHERE v.deleted = false
        AND (
            COALESCE(CAST(:status AS text), '') = ''
            OR v.status = CAST(:status AS text)
        )
        AND (
            COALESCE(CAST(:crop AS text), '') = ''
            OR v.crop ILIKE CONCAT('%', CAST(:crop AS text), '%')
        )
        AND (
            COALESCE(CAST(:region AS text), '') = ''
            OR v.region ILIKE CONCAT('%', CAST(:region AS text), '%')
            OR LOWER(v.region) = 'all zones'
        )
        AND (
            COALESCE(CAST(:season AS text), '') = ''
            OR v.season ILIKE CONCAT('%', CAST(:season AS text), '%')
            OR LOWER(v.season) = 'bimodal'
        )
        AND (
            COALESCE(CAST(:keyword AS text), '') = ''
            OR v.name ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            OR v.crop ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            OR v.description ILIKE CONCAT('%', CAST(:keyword AS text), '%')
        )
        ORDER BY v.created_at DESC
        """,
        countQuery = """
            SELECT COUNT(DISTINCT v.id)
            FROM varieties v
            WHERE v.deleted = false
            AND (
                COALESCE(CAST(:status AS text), '') = ''
                OR v.status = CAST(:status AS text)
            )
            AND (
                COALESCE(CAST(:crop AS text), '') = ''
                OR v.crop ILIKE CONCAT('%', CAST(:crop AS text), '%')
            )
            AND (
                COALESCE(CAST(:region AS text), '') = ''
                OR v.region ILIKE CONCAT('%', CAST(:region AS text), '%')
                OR LOWER(v.region) = 'all zones'
            )
            AND (
                COALESCE(CAST(:season AS text), '') = ''
                OR v.season ILIKE CONCAT('%', CAST(:season AS text), '%')
                OR LOWER(v.season) = 'bimodal'
            )
            AND (
                COALESCE(CAST(:keyword AS text), '') = ''
                OR v.name ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                OR v.crop ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                OR v.description ILIKE CONCAT('%', CAST(:keyword AS text), '%')
            )
            """,
        nativeQuery = true)
    Page<Variety> findWithFilters(
            @Param("status") String status,
            @Param("crop") String crop,
            @Param("region") String region,
            @Param("season") String season,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    /**
     * Get varieties belonging to a researcher.
     */
    @Query("""
        SELECT v
        FROM Variety v
        WHERE v.researcher.id = :researcherId
        AND v.deleted = false
        ORDER BY v.createdAt DESC
        """)
    List<Variety> findByResearcherIdOrderByCreatedAtDesc(
            @Param("researcherId") Long researcherId
    );


    /**
     * Get varieties by status.
     */
    @Query("""
        SELECT v
        FROM Variety v
        WHERE v.status = :status
        AND v.deleted = false
        ORDER BY v.createdAt ASC
        """)
    List<Variety> findByStatusOrderByCreatedAtAsc(
            @Param("status") VarietyStatus status
    );


    /**
     * Count varieties by status.
     */
    @Query("""
        SELECT COUNT(v)
        FROM Variety v
        WHERE v.status = :status
        AND v.deleted = false
        """)
    long countByStatus(
            @Param("status") VarietyStatus status
    );


    /**
     * Get the top-rated published varieties.
     *
     * Rating is calculated only for ordering.
     * It is not mapped into the Variety entity.
     */
    @Query(value = """
        SELECT v.*
        FROM varieties v
        WHERE v.status = 'PUBLISHED'
        AND v.deleted = false
        ORDER BY (
            SELECT COALESCE(AVG(r.score), 0)
            FROM ratings r
            WHERE r.variety_id = v.id
        ) DESC,
        v.created_at DESC
        LIMIT 6
        """,
        nativeQuery = true)
    List<Variety> findTopRatedPublished();


    /**
     * Check whether a non-deleted variety with the same name exists.
     */
    @Query("""
        SELECT COUNT(v) > 0
        FROM Variety v
        WHERE LOWER(v.name) = LOWER(:name)
        AND v.deleted = false
        """)
    boolean existsByNameIgnoreCase(
            @Param("name") String name
    );


    /**
     * Search published varieties by keyword.
     */
    @Query("""
        SELECT v
        FROM Variety v
        WHERE v.deleted = false
        AND v.status = 'PUBLISHED'
        AND (
            LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(v.crop) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY v.createdAt DESC
        """)
    List<Variety> searchPublishedVarieties(
            @Param("keyword") String keyword
    );


    /**
     * Get published varieties by region.
     *
     * "All Zones" varieties are included for every region.
     */
    @Query("""
        SELECT v
        FROM Variety v
        WHERE v.deleted = false
        AND v.status = 'PUBLISHED'
        AND (
            LOWER(v.region) = LOWER(:region)
            OR LOWER(v.region) = 'all zones'
        )
        ORDER BY v.createdAt DESC
        """)
    List<Variety> findByRegion(
            @Param("region") String region
    );


    /**
     * Get recently published varieties.
     */
    @Query("""
        SELECT v
        FROM Variety v
        WHERE v.deleted = false
        AND v.status = 'PUBLISHED'
        ORDER BY v.createdAt DESC
        """)
    List<Variety> findRecentPublished(
            Pageable pageable
    );


    /**
     * Get varieties awaiting administrator review.
     */
    @Query("""
        SELECT v
        FROM Variety v
        WHERE v.deleted = false
        AND v.status = 'PENDING'
        ORDER BY v.createdAt ASC
        """)
    List<Variety> findPendingForReview();


    /**
     * Count varieties grouped by crop.
     */
    @Query("""
        SELECT v.crop, COUNT(v)
        FROM Variety v
        WHERE v.deleted = false
        GROUP BY v.crop
        ORDER BY COUNT(v) DESC
        """)
    List<Object[]> countVarietiesByCrop();
}