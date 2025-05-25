package org.example.tourplannerbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Summary DTO for statistics
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourLogSummaryDto {

    private Long tourId;
    private String tourName;
    private Integer totalLogs;
    private Double averageDistance;
    private Double averageTime;
    private Double averageRating;
    private Double averageDifficulty;
    private LocalDateTime firstLogDate;
    private LocalDateTime lastLogDate;
}
