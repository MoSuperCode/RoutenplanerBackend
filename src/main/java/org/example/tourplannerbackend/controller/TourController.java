package org.example.tourplannerbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.dto.*;
import org.example.tourplannerbackend.service.TourService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // For frontend integration
public class TourController {

    private final TourService tourService;

    /**
     * Get all tours
     */
    @GetMapping
    public ResponseEntity<List<TourSummaryDto>> getAllTours() {
        log.info("GET /api/tours - Getting all tours");
        List<TourSummaryDto> tours = tourService.getAllTours();
        return ResponseEntity.ok(tours);
    }

    /**
     * Get tour by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TourResponseDto> getTourById(@PathVariable Long id) {
        log.info("GET /api/tours/{} - Getting tour by ID", id);
        TourResponseDto tour = tourService.getTourById(id);
        return ResponseEntity.ok(tour);
    }

    /**
     * Create new tour
     */
    @PostMapping
    public ResponseEntity<TourResponseDto> createTour(@Valid @RequestBody TourRequestDto tourRequestDto) {
        log.info("POST /api/tours - Creating new tour: {}", tourRequestDto.getName());
        TourResponseDto createdTour = tourService.createTour(tourRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTour);
    }

    /**
     * Update existing tour
     */
    @PutMapping("/{id}")
    public ResponseEntity<TourResponseDto> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourRequestDto tourRequestDto) {
        log.info("PUT /api/tours/{} - Updating tour", id);
        TourResponseDto updatedTour = tourService.updateTour(id, tourRequestDto);
        return ResponseEntity.ok(updatedTour);
    }

    /**
     * Delete tour
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        log.info("DELETE /api/tours/{} - Deleting tour", id);
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search tours (Must-Have: Full-text search)
     */
    @GetMapping("/search")
    public ResponseEntity<List<TourSummaryDto>> searchTours(@RequestParam("q") String searchTerm) {
        log.info("GET /api/tours/search?q={} - Searching tours", searchTerm);
        List<TourSummaryDto> tours = tourService.searchTours(searchTerm);
        return ResponseEntity.ok(tours);
    }

    /**
     * Get tours by transport type
     */
    @GetMapping("/transport/{transportType}")
    public ResponseEntity<List<TourSummaryDto>> getToursByTransportType(@PathVariable String transportType) {
        log.info("GET /api/tours/transport/{} - Getting tours by transport type", transportType);
        List<TourSummaryDto> tours = tourService.getToursByTransportType(transportType);
        return ResponseEntity.ok(tours);
    }

    /**
     * Get tours by location
     */
    @GetMapping("/location")
    public ResponseEntity<List<TourSummaryDto>> getToursByLocation(@RequestParam String location) {
        log.info("GET /api/tours/location?location={} - Getting tours by location", location);
        List<TourSummaryDto> tours = tourService.getToursByLocation(location);
        return ResponseEntity.ok(tours);
    }

    /**
     * Calculate route for existing tour
     */
    @PostMapping("/{id}/calculate-route")
    public ResponseEntity<TourResponseDto> calculateRoute(@PathVariable Long id) {
        log.info("POST /api/tours/{}/calculate-route - Calculating route", id);
        TourResponseDto tour = tourService.calculateRoute(id);
        return ResponseEntity.ok(tour);
    }

    /**
     * Get tour statistics
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<TourStatisticsDto> getTourStatistics(@PathVariable Long id) {
        log.info("GET /api/tours/{}/statistics - Getting statistics", id);
        TourStatisticsDto statistics = tourService.getTourStatistics(id);
        return ResponseEntity.ok(statistics);
    }
}