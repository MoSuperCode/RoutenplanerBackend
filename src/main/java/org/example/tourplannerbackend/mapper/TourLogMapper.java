package org.example.tourplannerbackend.mapper;

import org.example.tourplannerbackend.dto.TourLogRequestDto;
import org.example.tourplannerbackend.dto.TourLogResponseDto;
import org.example.tourplannerbackend.entity.TourLog;
import org.springframework.stereotype.Component;

@Component
public class TourLogMapper {

    /**
     * Convert TourLogRequestDto to TourLog entity
     */
    public TourLog toEntity(TourLogRequestDto dto) {
        if (dto == null) {
            return null;
        }

        TourLog tourLog = new TourLog();
        tourLog.setDate(dto.getDate());
        tourLog.setComment(dto.getComment() != null ? dto.getComment() : "");
        tourLog.setDifficulty(dto.getDifficulty());
        tourLog.setTotalDistance(dto.getTotalDistance());
        tourLog.setTotalTime(dto.getTotalTime());
        tourLog.setRating(dto.getRating());

        return tourLog;
    }

    /**
     * Convert TourLog entity to TourLogResponseDto
     */
    public TourLogResponseDto toResponseDto(TourLog tourLog) {
        if (tourLog == null) {
            return null;
        }

        return TourLogResponseDto.builder()
                .id(tourLog.getId())
                .date(tourLog.getDate())
                .comment(tourLog.getComment())
                .difficulty(tourLog.getDifficulty())
                .totalDistance(tourLog.getTotalDistance())
                .totalTime(tourLog.getTotalTime())
                .rating(tourLog.getRating())
                .createdAt(tourLog.getCreatedAt())
                .updatedAt(tourLog.getUpdatedAt())
                .tourId(tourLog.getTour() != null ? tourLog.getTour().getId() : null)
                .tourName(tourLog.getTour() != null ? tourLog.getTour().getName() : null)
                .build();
    }

    /**
     * Update existing TourLog entity from TourLogRequestDto
     */
    public void updateEntityFromDto(TourLogRequestDto dto, TourLog tourLog) {
        if (dto == null || tourLog == null) {
            return;
        }

        tourLog.setDate(dto.getDate());
        tourLog.setComment(dto.getComment() != null ? dto.getComment() : "");
        tourLog.setDifficulty(dto.getDifficulty());
        tourLog.setTotalDistance(dto.getTotalDistance());
        tourLog.setTotalTime(dto.getTotalTime());
        tourLog.setRating(dto.getRating());
    }
}