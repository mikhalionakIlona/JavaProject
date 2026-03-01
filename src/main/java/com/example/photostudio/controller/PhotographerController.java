package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotographerDto;
import com.example.photostudio.service.PhotographerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/photographers")
@RequiredArgsConstructor
public class PhotographerController {
    private final PhotographerService service;

    @GetMapping
    public ResponseEntity<List<PhotographerDto>> getAllPhotographers() {
        return ResponseEntity.ok(service.getAllPhotographers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotographerDto> getPhotographerById(@PathVariable Long id) {
        PhotographerDto photographer = service.getPhotographerById(id);
        return photographer == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(photographer);
    }
}