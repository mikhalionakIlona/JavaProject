package com.example.photostudio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer durationMinutes;

    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Photographer> photographers = new ArrayList<>();

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PhotoSession> photoSessions = new ArrayList<>();
}