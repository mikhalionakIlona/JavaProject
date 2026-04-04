package com.example.photostudio.controller;

import com.example.photostudio.service.async.RaceConditionDemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/race")
@RequiredArgsConstructor
public class RaceConditionController {

    private final RaceConditionDemoService raceConditionDemoService;

    @PostMapping("/problem")
    public ResponseEntity<String> demonstrateProblem() throws InterruptedException {
        String result = raceConditionDemoService.demonstrateRaceCondition();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/solution")
    public ResponseEntity<String> demonstrateSolution() throws InterruptedException {
        String result = raceConditionDemoService.demonstrateSolution();
        return ResponseEntity.ok(result);
    }
}