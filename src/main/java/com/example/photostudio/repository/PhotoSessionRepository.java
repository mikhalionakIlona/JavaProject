package com.example.photostudio.repository;

import com.example.photostudio.model.PhotoSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhotoSessionRepository extends JpaRepository<PhotoSession, Long> {

    List<PhotoSession> findByClientId(Long clientId);

    @Query("SELECT ps FROM PhotoSession ps")
    List<PhotoSession> findAllWithoutFetch();

    @EntityGraph(attributePaths = {"client", "photographer", "service"})
    @Query("SELECT ps FROM PhotoSession ps")
    List<PhotoSession> findAllWithEntityGraph();

    @Query(value = "SELECT DISTINCT ps FROM PhotoSession ps " +
            "LEFT JOIN ps.client c " +
            "LEFT JOIN ps.photographer p " +
            "LEFT JOIN ps.service s " +
            "WHERE (:clientName IS NULL OR " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :clientName, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
            "AND (:photographerName IS NULL OR " +
            "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :photographerName, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :photographerName, '%'))) " +
            "AND (:phone IS NULL OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :phone, '%')))")
    List<PhotoSession> findSessionsWithFilters(
            @Param("clientName") String clientName,
            @Param("photographerName") String photographerName,
            @Param("phone") String phone);

    @Query(value = "SELECT DISTINCT ps.* FROM photo_sessions ps " +
            "LEFT JOIN clients c ON ps.client_id = c.id " +
            "LEFT JOIN photographers p ON ps.photographer_id = p.id " +
            "LEFT JOIN services s ON ps.service_id = s.id " +
            "WHERE (:clientName IS NULL OR " +
            "LOWER(c.first_name) LIKE LOWER(CONCAT('%', :clientName, '%')) OR " +
            "LOWER(c.last_name) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
            "AND (:photographerName IS NULL OR " +
            "LOWER(p.first_name) LIKE LOWER(CONCAT('%', :photographerName, '%')) OR " +
            "LOWER(p.last_name) LIKE LOWER(CONCAT('%', :photographerName, '%'))) " +
            "AND (:phone IS NULL OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :phone, '%')))",
            nativeQuery = true)
    List<PhotoSession> findSessionsWithFiltersNative(
            @Param("clientName") String clientName,
            @Param("photographerName") String photographerName,
            @Param("phone") String phone);

    @Query(value = "SELECT DISTINCT ps.* FROM photo_sessions ps " +
            "LEFT JOIN clients c ON ps.client_id = c.id " +
            "LEFT JOIN photographers p ON ps.photographer_id = p.id " +
            "LEFT JOIN services s ON ps.service_id = s.id " +
            "WHERE (:clientName IS NULL OR " +
            "LOWER(c.first_name) LIKE LOWER(CONCAT('%', :clientName, '%')) OR " +
            "LOWER(c.last_name) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
            "AND (:photographerName IS NULL OR " +
            "LOWER(p.first_name) LIKE LOWER(CONCAT('%', :photographerName, '%')) OR " +
            "LOWER(p.last_name) LIKE LOWER(CONCAT('%', :photographerName, '%'))) " +
            "AND (:phone IS NULL OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) " +
            "ORDER BY ps.session_date DESC",
            countQuery = "SELECT COUNT(DISTINCT ps.id) FROM photo_sessions ps " +
                    "LEFT JOIN clients c ON ps.client_id = c.id " +
                    "LEFT JOIN photographers p ON ps.photographer_id = p.id " +
                    "WHERE (:clientName IS NULL OR " +
                    "LOWER(c.first_name) LIKE LOWER(CONCAT('%', :clientName, '%')) OR " +
                    "LOWER(c.last_name) LIKE LOWER(CONCAT('%', :clientName, '%'))) " +
                    "AND (:photographerName IS NULL OR " +
                    "LOWER(p.first_name) LIKE LOWER(CONCAT('%', :photographerName, '%')) OR " +
                    "LOWER(p.last_name) LIKE LOWER(CONCAT('%', :photographerName, '%'))) " +
                    "AND (:phone IS NULL OR " +
                    "LOWER(c.phone) LIKE LOWER(CONCAT('%', :phone, '%')))",
            nativeQuery = true)
    Page<PhotoSession> findSessionsWithFiltersPaged(
            @Param("clientName") String clientName,
            @Param("photographerName") String photographerName,
            @Param("phone") String phone,
            Pageable pageable);
}