package org.example.tourplannerbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourExportDto {

    private Long id;
    private String name;
    private String description;
    private String fromLocation;
    private String toLocation;
    private String transportType;
    private Double distance;
    private Integer estimatedTime;
    private String routeImagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TourLogExportDto> tourLogs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourLogExportDto {
        private Long id;
        private LocalDateTime date;
        private String comment;
        private Integer difficulty;
        private Double totalDistance;
        private Integer totalTime;
        private Integer rating;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}