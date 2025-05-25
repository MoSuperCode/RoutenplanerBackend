package org.example.tourplannerbackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.dto.RouteCalculationRequestDto;
import org.example.tourplannerbackend.dto.RouteCalculationResponseDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    /**
     * Calculate route without saving to database
     * Must-Have: OpenRouteService integration
     */
    @PostMapping("/calculate")
    public ResponseEntity<RouteCalculationResponseDto> calculateRoute(
            @Valid @RequestBody RouteCalculationRequestDto request) {
        log.info("POST /api/routes/calculate - Calculating route from {} to {} using {}",
                request.getFromLocation(), request.getToLocation(), request.getTransportType());

        try {
            // Create temporary tour object for calculation
            Tour tempTour = new Tour();
            tempTour.setFromLocation(request.getFromLocation());
            tempTour.setToLocation(request.getToLocation());
            tempTour.setTransportType(request.getTransportType());

            // Calculate route
            Tour calculatedTour = routeService.calculateRoute(tempTour);

            RouteCalculationResponseDto response = RouteCalculationResponseDto.builder()
                    .fromLocation(calculatedTour.getFromLocation())
                    .toLocation(calculatedTour.getToLocation())
                    .transportType(calculatedTour.getTransportType())
                    .distance(calculatedTour.getDistance())
                    .estimatedTime(calculatedTour.getEstimatedTime())
                    .routeImagePath(calculatedTour.getRouteImagePath())
                    .success(true)
                    .message("Route calculated successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error calculating route: {}", e.getMessage());

            RouteCalculationResponseDto errorResponse = RouteCalculationResponseDto.builder()
                    .fromLocation(request.getFromLocation())
                    .toLocation(request.getToLocation())
                    .transportType(request.getTransportType())
                    .success(false)
                    .message("Failed to calculate route: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}