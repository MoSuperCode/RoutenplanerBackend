package org.example.tourplannerbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

// Request DTO for creating/updating tour logs
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourLogRequestDto {

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    private String comment;

    @NotNull(message = "Difficulty is required")
    @Min(value = 1, message = "Difficulty must be between 1 and 10")
    @Max(value = 10, message = "Difficulty must be between 1 and 10")
    private Integer difficulty;

    @NotNull(message = "Total distance is required")
    @Positive(message = "Total distance must be positive")
    private Double totalDistance;

    @NotNull(message = "Total time is required")
    @Positive(message = "Total time must be positive")
    private Integer totalTime;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
}

