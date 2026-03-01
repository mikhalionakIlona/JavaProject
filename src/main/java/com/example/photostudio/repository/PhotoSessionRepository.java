package com.example.photostudio.repository;

import com.example.photostudio.model.PhotoSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhotoSessionRepository extends JpaRepository<PhotoSession, Long> {

    List<PhotoSession> findByClientNameIgnoreCase(String clientName);

    // Демонстрация N+1 проблемы
    @Query("SELECT ps FROM PhotoSession ps")
    List<PhotoSession> findAllWithoutFetch();

    // Решение через JOIN FETCH
    @Query("SELECT DISTINCT ps FROM PhotoSession ps " +
            "LEFT JOIN FETCH ps.client " +
            "LEFT JOIN FETCH ps.photographerEntity " +
            "LEFT JOIN FETCH ps.service")
    List<PhotoSession> findAllWithJoinFetch();

    // Решение через EntityGraph
    @EntityGraph(attributePaths = {"client", "photographerEntity", "service"})
    @Query("SELECT ps FROM PhotoSession ps")
    List<PhotoSession> findAllWithEntityGraph();
}