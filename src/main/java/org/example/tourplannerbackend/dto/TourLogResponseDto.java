package org.example.tourplannerbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Response DTO for tour log data
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourLogResponseDto {

    private Long id;
    private LocalDateTime date;
    private String comment;
    private Integer difficulty;
    private Double totalDistance;
    private Integer totalTime;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long tourId; // Reference to parent tour
    private String tourName; // Optional: tour name for convenience
}
