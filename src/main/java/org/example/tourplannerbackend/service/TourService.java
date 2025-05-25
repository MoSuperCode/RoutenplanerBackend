package org.example.tourplannerbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.dto.TourRequestDto;
import org.example.tourplannerbackend.dto.TourResponseDto;
import org.example.tourplannerbackend.dto.TourStatisticsDto;
import org.example.tourplannerbackend.dto.TourSummaryDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.exception.ResourceNotFoundException;
import org.example.tourplannerbackend.mapper.TourMapper;
import org.example.tourplannerbackend.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TourService {

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;
    private final RouteService routeService; // To be implemented

    /**
     * Get all tours with summary information
     */
    @Transactional(readOnly = true)
    public List<TourSummaryDto> getAllTours() {
        log.info("Fetching all tours");
        List<Tour> tours = tourRepository.findAll();
        log.info("Found {} tours", tours.size());

        return tours.stream()
                .map(tourMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Get tour by ID with full details
     */
    @Transactional(readOnly = true)
    public TourResponseDto getTourById(Long id) {
        log.info("Fetching tour with ID: {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        return tourMapper.toResponseDto(tour);
    }

    /**
     * Create new tour
     */
    public TourResponseDto createTour(TourRequestDto tourRequestDto) {
        log.info("Creating new tour: {}", tourRequestDto.getName());

        Tour tour = tourMapper.toEntity(tourRequestDto);

        // If distance and time are not provided, try to calculate them
        if ((tour.getDistance() == null || tour.getDistance() == 0) &&
                (tour.getEstimatedTime() == null || tour.getEstimatedTime() == 0)) {
            try {
                tour = routeService.calculateRoute(tour);
            } catch (Exception e) {
                log.warn("Could not calculate route for tour: {}", e.getMessage());
                // Continue with manual values or defaults
                if (tour.getDistance() == null) tour.setDistance(0.0);
                if (tour.getEstimatedTime() == null) tour.setEstimatedTime(0);
            }
        }

        Tour savedTour = tourRepository.save(tour);
        log.info("Tour created with ID: {}", savedTour.getId());

        return tourMapper.toResponseDto(savedTour);
    }

    /**
     * Update existing tour
     */
    public TourResponseDto updateTour(Long id, TourRequestDto tourRequestDto) {
        log.info("Updating tour with ID: {}", id);

        Tour existingTour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        // Update fields
        tourMapper.updateEntityFromDto(tourRequestDto, existingTour);

        // Check if route needs recalculation
        if (routeDataChanged(existingTour, tourRequestDto)) {
            try {
                existingTour = routeService.calculateRoute(existingTour);
            } catch (Exception e) {
                log.warn("Could not recalculate route for tour: {}", e.getMessage());
            }
        }

        Tour updatedTour = tourRepository.save(existingTour);
        log.info("Tour updated: {}", updatedTour.getId());

        return tourMapper.toResponseDto(updatedTour);
    }

    /**
     * Delete tour
     */
    public void deleteTour(Long id) {
        log.info("Deleting tour with ID: {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        tourRepository.delete(tour);
        log.info("Tour deleted: {}", id);
    }

    /**
     * Search tours with full-text search (Must-Have requirement)
     */
    @Transactional(readOnly = true)
    public List<TourSummaryDto> searchTours(String searchTerm) {
        log.info("Searching tours with term: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllTours();
        }

        List<Tour> tours = tourRepository.findByComprehensiveSearch(searchTerm.trim());
        log.info("Found {} tours matching search term", tours.size());

        return tours.stream()
                .map(tourMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Get tours by transport type
     */
    @Transactional(readOnly = true)
    public List<TourSummaryDto> getToursByTransportType(String transportType) {
        log.info("Fetching tours by transport type: {}", transportType);
        List<Tour> tours = tourRepository.findByTransportTypeIgnoreCase(transportType);

        return tours.stream()
                .map(tourMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Get tours by location (from or to)
     */
    @Transactional(readOnly = true)
    public List<TourSummaryDto> getToursByLocation(String location) {
        log.info("Fetching tours by location: {}", location);
        List<Tour> tours = tourRepository.findByLocation(location);

        return tours.stream()
                .map(tourMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Calculate route for existing tour
     */
    public TourResponseDto calculateRoute(Long id) {
        log.info("Calculating route for tour ID: {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        try {
            tour = routeService.calculateRoute(tour);
            Tour updatedTour = tourRepository.save(tour);
            log.info("Route calculated and saved for tour: {}", id);

            return tourMapper.toResponseDto(updatedTour);
        } catch (Exception e) {
            log.error("Failed to calculate route for tour {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to calculate route: " + e.getMessage());
        }
    }

    /**
     * Check if route-relevant data has changed
     */
    private boolean routeDataChanged(Tour existingTour, TourRequestDto dto) {
        return !existingTour.getFromLocation().equals(dto.getFromLocation()) ||
                !existingTour.getToLocation().equals(dto.getToLocation()) ||
                !existingTour.getTransportType().equals(dto.getTransportType());
    }

    /**
     * Get tour statistics
     */
    @Transactional(readOnly = true)
    public TourStatisticsDto getTourStatistics(Long id) {
        log.info("Fetching statistics for tour ID: {}", id);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + id));

        return TourStatisticsDto.builder()
                .tourId(id)
                .tourName(tour.getName())
                .totalLogs(tour.getTourLogs().size())
                .popularity(tour.getPopularity())
                .childFriendliness(tour.getChildFriendliness())
                .averageDistance(tour.getTourLogs().stream()
                        .mapToDouble(log -> log.getTotalDistance())
                        .average().orElse(0.0))
                .averageTime(tour.getTourLogs().stream()
                        .mapToDouble(log -> log.getTotalTime())
                        .average().orElse(0.0))
                .averageRating(tour.getTourLogs().stream()
                        .mapToDouble(log -> log.getRating())
                        .average().orElse(0.0))
                .averageDifficulty(tour.getTourLogs().stream()
                        .mapToDouble(log -> log.getDifficulty())
                        .average().orElse(0.0))
                .build();
    }
}