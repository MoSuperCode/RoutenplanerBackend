package org.example.tourplannerbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Data
@EqualsAndHashCode(exclude = "tourLogs")
@ToString(exclude = "tourLogs")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tour name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Starting location is required")
    @Column(nullable = false)
    private String fromLocation;

    @NotBlank(message = "Destination is required")
    @Column(nullable = false)
    private String toLocation;

    @NotBlank(message = "Transport type is required")
    @Column(nullable = false)
    private String transportType;

    @PositiveOrZero(message = "Distance must be positive")
    private Double distance; // kilometers

    @PositiveOrZero(message = "Estimated time must be positive")
    private Integer estimatedTime; // minutes

    private String routeImagePath;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourLog> tourLogs = new ArrayList<>();

    // Computed properties
    @Transient
    public int getPopularity() {
        return tourLogs != null ? tourLogs.size() : 0;
    }

    @Transient
    public double getChildFriendliness() {
        if (tourLogs == null || tourLogs.isEmpty()) {
            return 0;
        }

        double avgDifficulty = tourLogs.stream()
                .mapToInt(TourLog::getDifficulty)
                .average()
                .orElse(0);

        double avgTime = tourLogs.stream()
                .mapToInt(TourLog::getTotalTime)
                .average()
                .orElse(0);

        // Child-friendliness calculation: lower difficulty, distance and time = more child-friendly
        double rawScore = (avgDifficulty * 0.5) + ((distance != null ? distance : 0) * 0.3) + (avgTime * 0.2);

        // Convert to 0-10 scale where 10 is most child-friendly
        return Math.max(0, Math.min(10, 10 - rawScore));
    }

    // Helper methods
    public void addTourLog(TourLog tourLog) {
        if (tourLogs == null) {
            tourLogs = new ArrayList<>();
        }
        tourLogs.add(tourLog);
        tourLog.setTour(this);
    }

    public void removeTourLog(TourLog tourLog) {
        if (tourLogs != null) {
            tourLogs.remove(tourLog);
            tourLog.setTour(null);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}