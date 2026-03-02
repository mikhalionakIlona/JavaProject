package com.example.photostudio.controller;

import com.example.photostudio.dto.ServiceDto;
import com.example.photostudio.dto.ServiceCreateDto;
import com.example.photostudio.dto.ServiceUpdateDto;
import com.example.photostudio.service.PhotoServiceService;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {
    private final PhotoServiceService service;

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        return ResponseEntity.ok(service.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable Long id) {
        ServiceDto serviceDto = service.getServiceById(id);
        return serviceDto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(serviceDto);
    }

    @PostMapping
    public ResponseEntity<ServiceDto> createService(@RequestBody ServiceCreateDto createDto) {
        return ResponseEntity.ok(service.createService(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDto> updateService(@PathVariable Long id, @RequestBody ServiceUpdateDto updateDto) {
        ServiceDto updated = service.updateService(id, updateDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        return service.deleteService(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}