package org.example.tourplannerbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.dto.TourExportDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.entity.TourLog;
import org.example.tourplannerbackend.exception.ResourceNotFoundException;
import org.example.tourplannerbackend.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {

    private final TourRepository tourRepository;
    private final ObjectMapper objectMapper;

    /**
     * Export all tours to JSON format
     * Must-Have: Import/Export functionality
     */
    @Transactional(readOnly = true)
    public byte[] exportToursToJson() {
        log.info("Exporting all tours to JSON");

        try {
            List<Tour> tours = tourRepository.findAll();
            List<TourExportDto> exportDtos = tours.stream()
                    .map(this::convertToExportDto)
                    .collect(Collectors.toList());

            ObjectMapper mapper = createObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(exportDtos);

        } catch (Exception e) {
            log.error("Error exporting tours to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to export tours", e);
        }
    }

    /**
     * Export specific tour to JSON format
     */
    @Transactional(readOnly = true)
    public byte[] exportTourToJson(Long tourId) {
        log.info("Exporting tour {} to JSON", tourId);

        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + tourId));

            TourExportDto exportDto = convertToExportDto(tour);

            ObjectMapper mapper = createObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(exportDto);

        } catch (Exception e) {
            log.error("Error exporting tour {} to JSON: {}", tourId, e.getMessage());
            throw new RuntimeException("Failed to export tour", e);
        }
    }

    /**
     * Import tours from JSON file
     * Must-Have: Import/Export functionality
     */
    @Transactional
    public int importToursFromJson(MultipartFile file) {
        log.info("Importing tours from JSON file: {}", file.getOriginalFilename());

        try {
            ObjectMapper mapper = createObjectMapper();
            List<TourExportDto> exportDtos = mapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<TourExportDto>>() {}
            );

            int importedCount = 0;
            for (TourExportDto dto : exportDtos) {
                try {
                    Tour tour = convertFromExportDto(dto);
                    tourRepository.save(tour);
                    importedCount++;
                    log.debug("Imported tour: {}", tour.getName());
                } catch (Exception e) {
                    log.warn("Failed to import tour {}: {}", dto.getName(), e.getMessage());
                }
            }

            log.info("Successfully imported {} out of {} tours", importedCount, exportDtos.size());
            return importedCount;

        } catch (Exception e) {
            log.error("Error importing tours from JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to import tours", e);
        }
    }

    /**
     * Export tours to CSV format
     */
    @Transactional(readOnly = true)
    public byte[] exportToursToCsv() {
        log.info("Exporting tours to CSV");

        try {
            List<Tour> tours = tourRepository.findAll();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

            // Write CSV header
            writer.write("ID,Name,Description,From,To,Transport Type,Distance (km),Estimated Time (min),Created At,Tour Logs Count,Popularity,Child Friendliness\n");

            // Write data rows
            for (Tour tour : tours) {
                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%d,\"%s\",%d,%d,%.2f\n",
                        tour.getId(),
                        escapeCsv(tour.getName()),
                        escapeCsv(tour.getDescription()),
                        escapeCsv(tour.getFromLocation()),
                        escapeCsv(tour.getToLocation()),
                        escapeCsv(tour.getTransportType()),
                        tour.getDistance(),
                        tour.getEstimatedTime(),
                        tour.getCreatedAt().toString(),
                        tour.getTourLogs().size(),
                        tour.getPopularity(),
                        tour.getChildFriendliness()
                ));
            }

            writer.flush();
            writer.close();

            log.info("Successfully exported {} tours to CSV", tours.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting tours to CSV: {}", e.getMessage());
            throw new RuntimeException("Failed to export tours to CSV", e);
        }
    }

    /**
     * Convert Tour entity to export DTO
     */
    private TourExportDto convertToExportDto(Tour tour) {
        return TourExportDto.builder()
                .id(tour.getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .fromLocation(tour.getFromLocation())
                .toLocation(tour.getToLocation())
                .transportType(tour.getTransportType())
                .distance(tour.getDistance())
                .estimatedTime(tour.getEstimatedTime())
                .routeImagePath(tour.getRouteImagePath())
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .tourLogs(tour.getTourLogs().stream()
                        .map(this::convertTourLogToExportDto)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convert TourLog entity to export DTO
     */
    private TourExportDto.TourLogExportDto convertTourLogToExportDto(TourLog tourLog) {
        return TourExportDto.TourLogExportDto.builder()
                .id(tourLog.getId())
                .date(tourLog.getDate())
                .comment(tourLog.getComment())
                .difficulty(tourLog.getDifficulty())
                .totalDistance(tourLog.getTotalDistance())
                .totalTime(tourLog.getTotalTime())
                .rating(tourLog.getRating())
                .createdAt(tourLog.getCreatedAt())
                .updatedAt(tourLog.getUpdatedAt())
                .build();
    }

    /**
     * Convert export DTO to Tour entity
     */
    private Tour convertFromExportDto(TourExportDto dto) {
        Tour tour = new Tour();
        // Don't set ID - let it be auto-generated
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(dto.getTransportType());
        tour.setDistance(dto.getDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());
        tour.setRouteImagePath(dto.getRouteImagePath());

        // Convert tour logs
        if (dto.getTourLogs() != null) {
            List<TourLog> tourLogs = new ArrayList<>();
            for (TourExportDto.TourLogExportDto logDto : dto.getTourLogs()) {
                TourLog tourLog = new TourLog();
                // Don't set ID - let it be auto-generated
                tourLog.setDate(logDto.getDate());
                tourLog.setComment(logDto.getComment());
                tourLog.setDifficulty(logDto.getDifficulty());
                tourLog.setTotalDistance(logDto.getTotalDistance());
                tourLog.setTotalTime(logDto.getTotalTime());
                tourLog.setRating(logDto.getRating());
                tourLog.setTour(tour);
                tourLogs.add(tourLog);
            }
            tour.setTourLogs(tourLogs);
        }

        return tour;
    }

    /**
     * Create ObjectMapper with proper configuration
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Escape CSV values
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}