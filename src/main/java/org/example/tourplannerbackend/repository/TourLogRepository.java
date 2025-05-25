// TourLog Repository
package org.example.tourplannerbackend.repository;

import org.example.tourplannerbackend.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TourLogRepository extends JpaRepository<TourLog, Long> {

    // Find all logs for a specific tour
    List<TourLog> findByTourIdOrderByDateDesc(Long tourId);

    // Find logs by tour and date range
    List<TourLog> findByTourIdAndDateBetween(Long tourId, LocalDateTime startDate, LocalDateTime endDate);

    // Find logs by difficulty range
    List<TourLog> findByTourIdAndDifficultyBetween(Long tourId, Integer minDifficulty, Integer maxDifficulty);

    // Find logs by rating
    List<TourLog> findByTourIdAndRating(Long tourId, Integer rating);

    // Statistical queries for summary reports
    @Query("SELECT AVG(tl.totalDistance) FROM TourLog tl WHERE tl.tour.id = :tourId")
    Double findAverageDistanceByTour(@Param("tourId") Long tourId);

    @Query("SELECT AVG(tl.totalTime) FROM TourLog tl WHERE tl.tour.id = :tourId")
    Double findAverageTimeByTour(@Param("tourId") Long tourId);

    @Query("SELECT AVG(tl.rating) FROM TourLog tl WHERE tl.tour.id = :tourId")
    Double findAverageRatingByTour(@Param("tourId") Long tourId);

    @Query("SELECT AVG(tl.difficulty) FROM TourLog tl WHERE tl.tour.id = :tourId")
    Double findAverageDifficultyByTour(@Param("tourId") Long tourId);

    // Count logs per tour
    @Query("SELECT COUNT(tl) FROM TourLog tl WHERE tl.tour.id = :tourId")
    Long countLogsByTour(@Param("tourId") Long tourId);

    // Find recent logs across all tours
    @Query("SELECT tl FROM TourLog tl ORDER BY tl.date DESC")
    List<TourLog> findRecentLogs();

    // Search in tour log comments
    @Query("SELECT tl FROM TourLog tl WHERE LOWER(tl.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TourLog> findByCommentContaining(@Param("searchTerm") String searchTerm);
}