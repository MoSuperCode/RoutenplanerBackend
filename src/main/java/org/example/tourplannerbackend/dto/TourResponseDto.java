package org.example.tourplannerbackend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// Response DTO for tour data
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourResponseDto {

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

    // Computed properties
    private Integer popularity;
    private Double childFriendliness;

    // Associated tour logs count
    private Integer tourLogsCount;

    // Include tour logs if needed
    private List<TourLogResponseDto> tourLogs;
}