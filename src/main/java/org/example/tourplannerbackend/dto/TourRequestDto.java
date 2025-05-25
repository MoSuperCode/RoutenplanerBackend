package org.example.tourplannerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;



// Request DTO for creating/updating tours
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourRequestDto {

    @NotBlank(message = "Tour name is required")
    private String name;

    private String description;

    @NotBlank(message = "Starting location is required")
    private String fromLocation;

    @NotBlank(message = "Destination is required")
    private String toLocation;

    @NotBlank(message = "Transport type is required")
    private String transportType;

    @PositiveOrZero(message = "Distance must be positive or zero")
    private Double distance;

    @PositiveOrZero(message = "Estimated time must be positive or zero")
    private Integer estimatedTime;

    private String routeImagePath;
}

