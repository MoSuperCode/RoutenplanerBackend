package org.example.tourplannerbackend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourStatisticsDto {

    private Long tourId;
    private String tourName;
    private Integer totalLogs;
    private Integer popularity;
    private Double childFriendliness;
    private Double averageDistance;
    private Double averageTime;
    private Double averageRating;
    private Double averageDifficulty;
}