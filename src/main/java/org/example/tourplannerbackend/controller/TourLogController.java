package org.example.tourplannerbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.dto.TourLogRequestDto;
import org.example.tourplannerbackend.dto.TourLogResponseDto;
import org.example.tourplannerbackend.dto.TourLogSummaryDto;
import org.example.tourplannerbackend.service.TourLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // For frontend integration
public class TourLogController {

    private final TourLogService tourLogService;

    /**
     * Get all logs for a specific tour
     */
    @GetMapping("/tours/{tourId}/logs")
    public ResponseEntity<List<TourLogResponseDto>> getTourLogs(@PathVariable Long tourId) {
        log.info("GET /api/tours/{}/logs - Getting logs for tour", tourId);
        List<TourLogResponseDto> logs = tourLogService.getTourLogs(tourId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get tour log by ID
     */
    @GetMapping("/logs/{id}")
    public ResponseEntity<TourLogResponseDto> getTourLogById(@PathVariable Long id) {
        log.info("GET /api/logs/{} - Getting tour log by ID", id);
        TourLogResponseDto log = tourLogService.getTourLogById(id);
        return ResponseEntity.ok(log);
    }

    /**
     * Create new tour log
     */
    @PostMapping("/tours/{tourId}/logs")
    public ResponseEntity<TourLogResponseDto> createTourLog(
            @PathVariable Long tourId,
            @Valid @RequestBody TourLogRequestDto tourLogRequestDto) {
        log.info("POST /api/tours/{}/logs - Creating new tour log", tourId);
        TourLogResponseDto createdLog = tourLogService.createTourLog(tourId, tourLogRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLog);
    }

    /**
     * Update existing tour log
     */
    @PutMapping("/logs/{id}")
    public ResponseEntity<TourLogResponseDto> updateTourLog(
            @PathVariable Long id,
            @Valid @RequestBody TourLogRequestDto tourLogRequestDto) {
        log.info("PUT /api/logs/{} - Updating tour log", id);
        TourLogResponseDto updatedLog = tourLogService.updateTourLog(id, tourLogRequestDto);
        return ResponseEntity.ok(updatedLog);
    }

    /**
     * Delete tour log
     */
    @DeleteMapping("/logs/{id}")
    public ResponseEntity<Void> deleteTourLog(@PathVariable Long id) {
        log.info("DELETE /api/logs/{} - Deleting tour log", id);
        tourLogService.deleteTourLog(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get logs by date range
     */
    @GetMapping("/tours/{tourId}/logs/date-range")
    public ResponseEntity<List<TourLogResponseDto>> getTourLogsByDateRange(
            @PathVariable Long tourId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /api/tours/{}/logs/date-range - Getting logs between {} and {}", tourId, startDate, endDate);
        List<TourLogResponseDto> logs = tourLogService.getTourLogsByDateRange(tourId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs by difficulty range
     */
    @GetMapping("/tours/{tourId}/logs/difficulty")
    public ResponseEntity<List<TourLogResponseDto>> getTourLogsByDifficulty(
            @PathVariable Long tourId,
            @RequestParam Integer minDifficulty,
            @RequestParam Integer maxDifficulty) {
        log.info("GET /api/tours/{}/logs/difficulty - Getting logs with difficulty between {} and {}",
                tourId, minDifficulty, maxDifficulty);
        List<TourLogResponseDto> logs = tourLogService.getTourLogsByDifficulty(tourId, minDifficulty, maxDifficulty);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs by rating
     */
    @GetMapping("/tours/{tourId}/logs/rating/{rating}")
    public ResponseEntity<List<TourLogResponseDto>> getTourLogsByRating(
            @PathVariable Long tourId,
            @PathVariable Integer rating) {
        log.info("GET /api/tours/{}/logs/rating/{} - Getting logs with rating", tourId, rating);
        List<TourLogResponseDto> logs = tourLogService.getTourLogsByRating(tourId, rating);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get tour log summary for a specific tour
     */
    @GetMapping("/tours/{tourId}/logs/summary")
    public ResponseEntity<TourLogSummaryDto> getTourLogSummary(@PathVariable Long tourId) {
        log.info("GET /api/tours/{}/logs/summary - Getting tour log summary", tourId);
        TourLogSummaryDto summary = tourLogService.getTourLogSummary(tourId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Search tour logs by comment
     */
    @GetMapping("/logs/search")
    public ResponseEntity<List<TourLogResponseDto>> searchTourLogsByComment(@RequestParam("q") String searchTerm) {
        log.info("GET /api/logs/search?q={} - Searching tour logs by comment", searchTerm);
        List<TourLogResponseDto> logs = tourLogService.searchTourLogsByComment(searchTerm);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get recent tour logs across all tours
     */
    @GetMapping("/logs/recent")
    public ResponseEntity<List<TourLogResponseDto>> getRecentTourLogs(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/logs/recent?limit={} - Getting recent tour logs", limit);
        List<TourLogResponseDto> logs = tourLogService.getRecentTourLogs(limit);
        return ResponseEntity.ok(logs);
    }
}