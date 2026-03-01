package com.example.photostudio.repository;

import com.example.photostudio.model.PhotoService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<PhotoService, Long> {
}