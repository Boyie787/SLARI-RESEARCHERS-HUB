package slari.researchers.repository;

import slari.researchers.entity.User;
import slari.researchers.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndActiveTrue(Role role);

    long countByRole(Role role);

    /**
     * Search users by name or email (for admin panel).
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    /**
     * Search users with pagination.
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Get active users by role with pagination.
     */
    Page<User> findByRoleAndActiveTrue(Role role, Pageable pageable);

    /**
     * Count active users by role (for dashboard stats).
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.role")
    List<Object[]> countActiveUsersByRole();

    /**
     * Get users by location (for regional targeting).
     */
    List<User> findByLocationAndActiveTrue(String location);

    /**
     * Get extension officers by location (for farmer matching).
     */
    @Query("SELECT u FROM User u WHERE u.role = 'EXTENSION' AND u.location = :location AND u.active = true")
    List<User> findExtensionOfficersByLocation(@Param("location") String location);

    /**
     * Get users registered in the last N days.
     */
    //  CORRECT - Using native query
    @Query(value = "SELECT * FROM users u WHERE u.created_at > NOW() - INTERVAL :days DAY", nativeQuery = true)
    List<User> findUsersRegisteredInLastDays(@Param("days") int days);
}