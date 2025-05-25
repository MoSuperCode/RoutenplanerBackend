package org.example.tourplannerbackend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
// Summary DTO for list views
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourSummaryDto {

    private Long id;
    private String name;
    private String fromLocation;
    private String toLocation;
    private String transportType;
    private Double distance;
    private Integer estimatedTime;
    private Integer popularity;
    private Double childFriendliness;
    private LocalDateTime createdAt;
}