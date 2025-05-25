package org.example.tourplannerbackend.repository;

import org.example.tourplannerbackend.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    // Full-text search in tours (Must-Have requirement)
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN t.tourLogs tl WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.transportType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tl.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Tour> findBySearchTerm(@Param("searchTerm") String searchTerm);

    // Search in computed attributes as well
    @Query("SELECT t FROM Tour t WHERE " +
            "CAST(SIZE(t.tourLogs) AS STRING) LIKE CONCAT('%', :searchTerm, '%')")
    List<Tour> findByPopularity(@Param("searchTerm") String searchTerm);

    // Find tours by transport type
    List<Tour> findByTransportTypeIgnoreCase(String transportType);

    // Find tours by location
    @Query("SELECT t FROM Tour t WHERE " +
            "LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
            "LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Tour> findByLocation(@Param("location") String location);

    // Find tours with specific distance range
    List<Tour> findByDistanceBetween(Double minDistance, Double maxDistance);

    // Find tours with specific time range
    List<Tour> findByEstimatedTimeBetween(Integer minTime, Integer maxTime);

    // Custom query for comprehensive search including computed values
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN t.tourLogs tl WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.fromLocation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.toLocation) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.transportType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tl.comment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "CAST(t.distance AS STRING) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "CAST(t.estimatedTime AS STRING) LIKE CONCAT('%', :searchTerm, '%') OR " +
            "CAST(SIZE(t.tourLogs) AS STRING) LIKE CONCAT('%', :searchTerm, '%')")
    List<Tour> findByComprehensiveSearch(@Param("searchTerm") String searchTerm);
}

