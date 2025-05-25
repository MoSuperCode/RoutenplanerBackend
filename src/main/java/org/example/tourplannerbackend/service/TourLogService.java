package org.example.tourplannerbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.dto.TourLogRequestDto;
import org.example.tourplannerbackend.dto.TourLogResponseDto;
import org.example.tourplannerbackend.dto.TourLogSummaryDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.entity.TourLog;
import org.example.tourplannerbackend.exception.ResourceNotFoundException;
import org.example.tourplannerbackend.mapper.TourLogMapper;
import org.example.tourplannerbackend.repository.TourLogRepository;
import org.example.tourplannerbackend.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final TourLogMapper tourLogMapper;

    /**
     * Get all logs for a specific tour
     */
    @Transactional(readOnly = true)
    public List<TourLogResponseDto> getTourLogs(Long tourId) {
        log.info("Fetching logs for tour ID: {}", tourId);

        // Verify tour exists
        if (!tourRepository.existsById(tourId)) {
            throw new ResourceNotFoundException("Tour not found with ID: " + tourId);
        }

        List<TourLog> logs = tourLogRepository.findByTourIdOrderByDateDesc(tourId);
        log.info("Found {} logs for tour {}", logs.size(), tourId);

        return logs.stream()
                .map(tourLogMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get tour log by ID
     */
    @Transactional(readOnly = true)
    public TourLogResponseDto getTourLogById(Long id) {
        log.info("Fetching tour log with ID: {}", id);

        TourLog tourLog = tourLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with ID: " + id));

        return tourLogMapper.toResponseDto(tourLog);
    }

    /**
     * Create new tour log
     */
    public TourLogResponseDto createTourLog(Long tourId, TourLogRequestDto tourLogRequestDto) {
        log.info("Creating new log for tour ID: {}", tourId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + tourId));

        TourLog tourLog = tourLogMapper.toEntity(tourLogRequestDto);
        tourLog.setTour(tour);

        // Set current time if date is null
        if (tourLog.getDate() == null) {
            tourLog.setDate(LocalDateTime.now());
        }

        TourLog savedTourLog = tourLogRepository.save(tourLog);
        log.info("Tour log created with ID: {}", savedTourLog.getId());

        return tourLogMapper.toResponseDto(savedTourLog);
    }

    /**
     * Update existing tour log
     */
    public TourLogResponseDto updateTourLog(Long id, TourLogRequestDto tourLogRequestDto) {
        log.info("Updating tour log with ID: {}", id);

        TourLog existingTourLog = tourLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with ID: " + id));

        // Update fields
        tourLogMapper.updateEntityFromDto(tourLogRequestDto, existingTourLog);

        TourLog updatedTourLog = tourLogRepository.save(existingTourLog);
        log.info("Tour log updated: {}", updatedTourLog.getId());

        return tourLogMapper.toResponseDto(updatedTourLog);
    }

    /**
     * Delete tour log
     */
    public void deleteTourLog(Long id) {
        log.info("Deleting tour log with ID: {}", id);

        TourLog tourLog = tourLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with ID: " + id));

        tourLogRepository.delete(tourLog);
        log.info("Tour log deleted: {}", id);
    }

    /**
     * Get logs by date range
     */
    @Transactional(readOnly = true)
    public List<TourLogResponseDto> getTourLogsByDateRange(Long tourId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching logs for tour {} between {} and {}", tourId, startDate, endDate);

        if (!tourRepository.existsById(tourId)) {
            throw new ResourceNotFoundException("Tour not found with ID: " + tourId);
        }

        List<TourLog> logs = tourLogRepository.findByTourIdAndDateBetween(tourId, startDate, endDate);

        return logs.stream()
                .map(tourLogMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by difficulty range
     */
    @Transactional(readOnly = true)
    public List<TourLogResponseDto> getTourLogsByDifficulty(Long tourId, Integer minDifficulty, Integer maxDifficulty) {
        log.info("Fetching logs for tour {} with difficulty between {} and {}", tourId, minDifficulty, maxDifficulty);

        if (!tourRepository.existsById(tourId)) {
            throw new ResourceNotFoundException("Tour not found with ID: " + tourId);
        }

        List<TourLog> logs = tourLogRepository.findByTourIdAndDifficultyBetween(tourId, minDifficulty, maxDifficulty);

        return logs.stream()
                .map(tourLogMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by rating
     */
    @Transactional(readOnly = true)
    public List<TourLogResponseDto> getTourLogsByRating(Long tourId, Integer rating) {
        log.info("Fetching logs for tour {} with rating {}", tourId, rating);

        if (!tourRepository.existsById(tourId)) {
            throw new ResourceNotFoundException("Tour not found with ID: " + tourId);
        }

        List<TourLog> logs = tourLogRepository.findByTourIdAndRating(tourId, rating);

        return logs.stream()
                .map(tourLogMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get tour log summary for a specific tour
     */
    @Transactional(readOnly = true)
    public TourLogSummaryDto getTourLogSummary(Long tourId) {
        log.info("Fetching tour log summary for tour ID: {}", tourId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + tourId));

        Long totalLogs = tourLogRepository.countLogsByTour(tourId);
        Double avgDistance = tourLogRepository.findAverageDistanceByTour(tourId);
        Double avgTime = tourLogRepository.findAverageTimeByTour(tourId);
        Double avgRating = tourLogRepository.findAverageRatingByTour(tourId);
        Double avgDifficulty = tourLogRepository.findAverageDifficultyByTour(tourId);

        List<TourLog> logs = tourLogRepository.findByTourIdOrderByDateDesc(tourId);
        LocalDateTime firstDate = logs.isEmpty() ? null : logs.get(logs.size() - 1).getDate();
        LocalDateTime lastDate = logs.isEmpty() ? null : logs.get(0).getDate();

        return TourLogSummaryDto.builder()
                .tourId(tourId)
                .tourName(tour.getName())
                .totalLogs(totalLogs.intValue())
                .averageDistance(avgDistance != null ? avgDistance : 0.0)
                .averageTime(avgTime != null ? avgTime : 0.0)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .averageDifficulty(avgDifficulty != null ? avgDifficulty : 0.0)
                .firstLogDate(firstDate)
                .lastLogDate(lastDate)
                .build();
    }

    /**
     * Search tour logs by comment
     */
    @Transactional(readOnly = true)
    public List<TourLogResponseDto> searchTourLogsByComment(String searchTerm) {
        log.info("Searching tour logs with comment containing: '{}'", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return tourLogRepository.findRecentLogs().stream()
                    .map(tourLogMapper::toResponseDto)
                    .collect(Collectors.toList());
        }

        List<TourLog> logs = tourLogRepository.findByCommentContaining(searchTerm.trim());
        log.info("Found {} tour logs matching search term", logs.size());

        return logs.stream()
                .map(tourLogMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get recent tour logs across all tours
     */
    @Transactional(readOnly = true)
    public List<TourLogResponseDto> getRecentTourLogs(int limit) {
        log.info("Fetching {} most recent tour logs", limit);

        List<TourLog> logs = tourLogRepository.findRecentLogs();

        return logs.stream()
                .limit(limit)
                .map(tourLogMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
