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

    @Query("SELECT ps FROM PhotoSession ps")
    List<PhotoSession> findAllWithoutFetch();

    @EntityGraph(attributePaths = {"client", "photographerEntity", "service"})
    @Query("SELECT ps FROM PhotoSession ps")
    List<PhotoSession> findAllWithEntityGraph();
}