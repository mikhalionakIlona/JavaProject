package com.example.photostudio.repository;

import com.example.photostudio.model.PhotoSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}