package org.example.tourplannerbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteCalculationResponseDto {

    private String fromLocation;
    private String toLocation;
    private String transportType;
    private Double distance;
    private Integer estimatedTime;
    private String routeImagePath;
    private Boolean success;
    private String message;
}
