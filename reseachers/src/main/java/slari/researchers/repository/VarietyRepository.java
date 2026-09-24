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
     * Non-paginated version — used by the legacy browse() method
     * and any service method that needs a full list.
     */
    @Query(value = """
        SELECT DISTINCT
            v.*,
            CAST(
                ROUND(
                    CAST(
                        (SELECT AVG(r.score) FROM ratings r
                         WHERE r.variety_id = v.id)
                    AS numeric), 1
                )
            AS float8) AS average_rating,
            (SELECT COUNT(r.id) FROM ratings r
             WHERE r.variety_id = v.id) AS rating_count
        FROM varieties v
        LEFT JOIN users u ON u.id = v.researcher_id
        WHERE
            (CAST(:status AS text) IS NULL
                OR v.status = CAST(:status AS text))
        AND (CAST(:crop AS text) IS NULL
                OR v.crop ILIKE CAST(:crop AS text))
        AND (CAST(:region AS text) IS NULL
                OR v.region ILIKE CAST(:region AS text)
                OR lower(v.region) = 'all zones')
        AND (CAST(:season AS text) IS NULL
                OR v.season ILIKE CAST(:season AS text)
                OR lower(v.season) = 'bimodal')
        AND (CAST(:keyword AS text) IS NULL
                OR v.name        ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                OR v.crop        ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                OR v.description ILIKE CONCAT('%', CAST(:keyword AS text), '%'))
        ORDER BY v.created_at DESC
        """, nativeQuery = true)
    List<Variety> findWithFilters(
            @Param("status")  String status,
            @Param("crop")    String crop,
            @Param("region")  String region,
            @Param("season")  String season,
            @Param("keyword") String keyword
    );

    /**
     * Paginated version — used by the controller browse endpoint.
     *
     * countQuery is REQUIRED for native queries with Pageable.
     * Spring cannot derive the COUNT automatically from a native
     * SELECT DISTINCT with subqueries — it must be explicit.
     */
    @Query(
        value = """
            SELECT DISTINCT
                v.*,
                CAST(
                    ROUND(
                        CAST(
                            (SELECT AVG(r.score) FROM ratings r
                             WHERE r.variety_id = v.id)
                        AS numeric), 1
                    )
                AS float8) AS average_rating,
                (SELECT COUNT(r.id) FROM ratings r
                 WHERE r.variety_id = v.id) AS rating_count
            FROM varieties v
            LEFT JOIN users u ON u.id = v.researcher_id
            WHERE
                (CAST(:status AS text) IS NULL
                    OR v.status = CAST(:status AS text))
            AND (CAST(:crop AS text) IS NULL
                    OR v.crop ILIKE CAST(:crop AS text))
            AND (CAST(:region AS text) IS NULL
                    OR v.region ILIKE CAST(:region AS text)
                    OR lower(v.region) = 'all zones')
            AND (CAST(:season AS text) IS NULL
                    OR v.season ILIKE CAST(:season AS text)
                    OR lower(v.season) = 'bimodal')
            AND (CAST(:keyword AS text) IS NULL
                    OR v.name        ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR v.crop        ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR v.description ILIKE CONCAT('%', CAST(:keyword AS text), '%'))
            ORDER BY v.created_at DESC
            """,
        countQuery = """
            SELECT COUNT(DISTINCT v.id)
            FROM varieties v
            WHERE
                (CAST(:status AS text) IS NULL
                    OR v.status = CAST(:status AS text))
            AND (CAST(:crop AS text) IS NULL
                    OR v.crop ILIKE CAST(:crop AS text))
            AND (CAST(:region AS text) IS NULL
                    OR v.region ILIKE CAST(:region AS text)
                    OR lower(v.region) = 'all zones')
            AND (CAST(:season AS text) IS NULL
                    OR v.season ILIKE CAST(:season AS text)
                    OR lower(v.season) = 'bimodal')
            AND (CAST(:keyword AS text) IS NULL
                    OR v.name        ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR v.crop        ILIKE CONCAT('%', CAST(:keyword AS text), '%')
                    OR v.description ILIKE CONCAT('%', CAST(:keyword AS text), '%'))
            """,
        nativeQuery = true
    )
    Page<Variety> findWithFilters(
            @Param("status")  String status,
            @Param("crop")    String crop,
            @Param("region")  String region,
            @Param("season")  String season,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Variety> findByResearcherIdOrderByCreatedAtDesc(Long researcherId);

    List<Variety> findByStatusOrderByCreatedAtAsc(VarietyStatus status);

    long countByStatus(VarietyStatus status);

    @Query(value = """
        SELECT v.* FROM varieties v
        WHERE v.status = 'PUBLISHED'
        ORDER BY (
            SELECT COALESCE(CAST(AVG(r.score) AS numeric), 0)
            FROM ratings r WHERE r.variety_id = v.id
        ) DESC
        LIMIT 6
        """, nativeQuery = true)
    List<Variety> findTopRatedPublished();

    boolean existsByNameIgnoreCase(String name);
}