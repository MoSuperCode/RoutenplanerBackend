package org.example.tourplannerbackend.mapper;

import org.example.tourplannerbackend.dto.TourRequestDto;
import org.example.tourplannerbackend.dto.TourResponseDto;
import org.example.tourplannerbackend.dto.TourSummaryDto;
import org.example.tourplannerbackend.entity.Tour;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TourMapper {

    private final TourLogMapper tourLogMapper;

    public TourMapper(TourLogMapper tourLogMapper) {
        this.tourLogMapper = tourLogMapper;
    }

    /**
     * Convert TourRequestDto to Tour entity
     */
    public Tour toEntity(TourRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Tour tour = new Tour();
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(dto.getTransportType());
        tour.setDistance(dto.getDistance());
        tour.setEstimatedTime(dto.getEstimatedTime());
        tour.setRouteImagePath(dto.getRouteImagePath());

        return tour;
    }

    /**
     * Convert Tour entity to TourResponseDto
     */
    public TourResponseDto toResponseDto(Tour tour) {
        if (tour == null) {
            return null;
        }

        return TourResponseDto.builder()
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
                .popularity(tour.getPopularity())
                .childFriendliness(tour.getChildFriendliness())
                .tourLogsCount(tour.getTourLogs() != null ? tour.getTourLogs().size() : 0)
                .tourLogs(tour.getTourLogs() != null ?
                        tour.getTourLogs().stream()
                                .map(tourLogMapper::toResponseDto)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Convert Tour entity to TourSummaryDto
     */
    public TourSummaryDto toSummaryDto(Tour tour) {
        if (tour == null) {
            return null;
        }

        return TourSummaryDto.builder()
                .id(tour.getId())
                .name(tour.getName())
                .fromLocation(tour.getFromLocation())
                .toLocation(tour.getToLocation())
                .transportType(tour.getTransportType())
                .distance(tour.getDistance())
                .estimatedTime(tour.getEstimatedTime())
                .popularity(tour.getPopularity())
                .childFriendliness(tour.getChildFriendliness())
                .createdAt(tour.getCreatedAt())
                .build();
    }

    /**
     * Update existing Tour entity from TourRequestDto
     */
    public void updateEntityFromDto(TourRequestDto dto, Tour tour) {
        if (dto == null || tour == null) {
            return;
        }

        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setFromLocation(dto.getFromLocation());
        tour.setToLocation(dto.getToLocation());
        tour.setTransportType(dto.getTransportType());

        // Only update distance and time if provided (allow manual override)
        if (dto.getDistance() != null) {
            tour.setDistance(dto.getDistance());
        }
        if (dto.getEstimatedTime() != null) {
            tour.setEstimatedTime(dto.getEstimatedTime());
        }
        if (dto.getRouteImagePath() != null) {
            tour.setRouteImagePath(dto.getRouteImagePath());
        }
    }
}