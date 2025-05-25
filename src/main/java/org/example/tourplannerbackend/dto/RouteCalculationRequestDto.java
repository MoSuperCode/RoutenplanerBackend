package org.example.tourplannerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteCalculationRequestDto {

    @NotBlank(message = "Starting location is required")
    private String fromLocation;

    @NotBlank(message = "Destination is required")
    private String toLocation;

    @NotBlank(message = "Transport type is required")
    private String transportType;
}

