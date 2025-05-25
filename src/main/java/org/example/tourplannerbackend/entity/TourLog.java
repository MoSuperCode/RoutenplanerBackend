package org.example.tourplannerbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_logs")
@Data
@EqualsAndHashCode(exclude = "tour")
@ToString(exclude = "tour")
public class TourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDateTime date;

    @Column(columnDefinition = "TEXT")
    private String comment = "";

    @Min(value = 1, message = "Difficulty must be between 1 and 10")
    @Max(value = 10, message = "Difficulty must be between 1 and 10")
    @Column(nullable = false)
    private Integer difficulty;

    @Positive(message = "Total distance must be positive")
    @Column(nullable = false)
    private Double totalDistance; // kilometers

    @Positive(message = "Total time must be positive")
    @Column(nullable = false)
    private Integer totalTime; // minutes

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Column(nullable = false)
    private Integer rating;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    // Default constructor
    public TourLog() {
        this.date = LocalDateTime.now();
        this.difficulty = 5;
        this.rating = 3;
        this.comment = "";
    }

    // Constructor with required fields
    public TourLog(LocalDateTime date, String comment, Integer difficulty,
                   Double totalDistance, Integer totalTime, Integer rating) {
        this.date = date != null ? date : LocalDateTime.now();
        this.comment = comment != null ? comment : "";
        this.difficulty = difficulty != null ? difficulty : 5;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.rating = rating != null ? rating : 3;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Set defaults if null
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (comment == null) {
            comment = "";
        }
        if (difficulty == null) {
            difficulty = 5;
        }
        if (rating == null) {
            rating = 3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}