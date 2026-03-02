package com.example.photostudio.controller;

import com.example.photostudio.dto.PhotographerDto;
import com.example.photostudio.dto.PhotographerCreateDto;
import com.example.photostudio.dto.PhotographerUpdateDto;
import com.example.photostudio.service.PhotographerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping
    public ResponseEntity<PhotographerDto> createPhotographer(@RequestBody PhotographerCreateDto createDto) {
        return ResponseEntity.ok(service.createPhotographer(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotographerDto> updatePhotographer(@PathVariable Long id,
                                                              @RequestBody PhotographerUpdateDto updateDto) {
        PhotographerDto updated = service.updatePhotographer(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhotographer(@PathVariable Long id) {
        return service.deletePhotographer(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}