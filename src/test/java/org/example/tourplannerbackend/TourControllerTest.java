package org.example.tourplannerbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tourplannerbackend.dto.TourRequestDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TourController
 * Tests the complete flow from HTTP request to database
 * These tests are critical because they verify:
 * 1. End-to-end functionality of REST endpoints
 * 2. Database integration and transactions
 * 3. Request/Response serialization
 * 4. Validation and error handling
 * 5. Complete Spring Boot application context
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "logging.level.org.springframework.web=DEBUG"
})
@Transactional
public class TourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Tour testTour;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        tourRepository.deleteAll();

        // Create test data
        testTour = new Tour();
        testTour.setName("Vienna to Salzburg");
        testTour.setDescription("Beautiful scenic route through Austria");
        testTour.setFromLocation("Vienna");
        testTour.setToLocation("Salzburg");
        testTour.setTransportType("Car");
        testTour.setDistance(295.0);
        testTour.setEstimatedTime(180);
        testTour.setRouteImagePath("/images/vienna-salzburg.png");

        testTour = tourRepository.save(testTour);
    }

    // ==================== GET ALL TOURS TESTS ====================

    @Test
    void testGetAllTours_Success() throws Exception {
        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testTour.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Vienna to Salzburg")))
                .andExpect(jsonPath("$[0].fromLocation", is("Vienna")))
                .andExpect(jsonPath("$[0].toLocation", is("Salzburg")))
                .andExpect(jsonPath("$[0].transportType", is("Car")))
                .andExpect(jsonPath("$[0].distance", is(295.0)))
                .andExpect(jsonPath("$[0].estimatedTime", is(180)));
    }

    @Test
    void testGetAllTours_EmptyDatabase() throws Exception {
        // Clear all tours
        tourRepository.deleteAll();

        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetAllTours_MultipleTours() throws Exception {
        // Add another tour
        Tour secondTour = new Tour();
        secondTour.setName("Graz to Linz");
        secondTour.setDescription("Another scenic route");
        secondTour.setFromLocation("Graz");
        secondTour.setToLocation("Linz");
        secondTour.setTransportType("Bicycle");
        secondTour.setDistance(150.0);
        secondTour.setEstimatedTime(480);
        tourRepository.save(secondTour);

        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Vienna to Salzburg", "Graz to Linz")));
    }

    // ==================== GET TOUR BY ID TESTS ====================

    @Test
    void testGetTourById_Success() throws Exception {
        mockMvc.perform(get("/api/tours/{id}", testTour.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testTour.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Vienna to Salzburg")))
                .andExpect(jsonPath("$.description", is("Beautiful scenic route through Austria")))
                .andExpect(jsonPath("$.fromLocation", is("Vienna")))
                .andExpect(jsonPath("$.toLocation", is("Salzburg")))
                .andExpect(jsonPath("$.transportType", is("Car")))
                .andExpect(jsonPath("$.distance", is(295.0)))
                .andExpect(jsonPath("$.estimatedTime", is(180)))
                .andExpect(jsonPath("$.routeImagePath", is("/images/vienna-salzburg.png")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()))
                .andExpect(jsonPath("$.popularity", is(0)))
                .andExpect(jsonPath("$.childFriendliness", notNullValue()))
                .andExpect(jsonPath("$.tourLogsCount", is(0)));
    }

    @Test
    void testGetTourById_NotFound() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/tours/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Tour not found with ID: " + nonExistentId)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // ==================== CREATE TOUR TESTS ====================

    @Test
    void testCreateTour_Success() throws Exception {
        TourRequestDto newTour = TourRequestDto.builder()
                .name("Innsbruck to Klagenfurt")
                .description("Mountain route through Tyrol")
                .fromLocation("Innsbruck")
                .toLocation("Klagenfurt")
                .transportType("Car")
                .distance(300.0)
                .estimatedTime(240)
                .build();

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTour)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Innsbruck to Klagenfurt")))
                .andExpect(jsonPath("$.description", is("Mountain route through Tyrol")))
                .andExpect(jsonPath("$.fromLocation", is("Innsbruck")))
                .andExpect(jsonPath("$.toLocation", is("Klagenfurt")))
                .andExpect(jsonPath("$.transportType", is("Car")))
                .andExpect(jsonPath("$.distance", is(300.0)))
                .andExpect(jsonPath("$.estimatedTime", is(240)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void testCreateTour_ValidationError_EmptyName() throws Exception {
        TourRequestDto invalidTour = TourRequestDto.builder()
                .name("")  // Empty name should trigger validation error
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .build();

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTour)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.validationErrors", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.name", containsString("required")));
    }

    @Test
    void testCreateTour_ValidationError_MissingFromLocation() throws Exception {
        TourRequestDto invalidTour = TourRequestDto.builder()
                .name("Test Tour")
                .fromLocation("")  // Empty from location
                .toLocation("Salzburg")
                .transportType("Car")
                .build();

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTour)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.fromLocation", notNullValue()));
    }

    @Test
    void testCreateTour_ValidationError_MissingToLocation() throws Exception {
        TourRequestDto invalidTour = TourRequestDto.builder()
                .name("Test Tour")
                .fromLocation("Vienna")
                .toLocation("")  // Empty to location
                .transportType("Car")
                .build();

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTour)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.toLocation", notNullValue()));
    }

    @Test
    void testCreateTour_ValidationError_MissingTransportType() throws Exception {
        TourRequestDto invalidTour = TourRequestDto.builder()
                .name("Test Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("")  // Empty transport type
                .build();

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTour)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.transportType", notNullValue()));
    }

    @Test
    void testCreateTour_ValidationError_NegativeDistance() throws Exception {
        TourRequestDto invalidTour = TourRequestDto.builder()
                .name("Test Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .distance(-10.0)  // Negative distance
                .build();

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTour)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.distance", notNullValue()));
    }

    // ==================== UPDATE TOUR TESTS ====================

    @Test
    void testUpdateTour_Success() throws Exception {
        TourRequestDto updateRequest = TourRequestDto.builder()
                .name("Updated Vienna to Salzburg")
                .description("Updated description")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Bicycle")
                .distance(300.0)
                .estimatedTime(600)
                .build();

        mockMvc.perform(put("/api/tours/{id}", testTour.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testTour.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Vienna to Salzburg")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.transportType", is("Bicycle")))
                .andExpect(jsonPath("$.distance", is(300.0)))
                .andExpect(jsonPath("$.estimatedTime", is(600)))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void testUpdateTour_NotFound() throws Exception {
        Long nonExistentId = 999L;
        TourRequestDto updateRequest = TourRequestDto.builder()
                .name("Non-existent Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .build();

        mockMvc.perform(put("/api/tours/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Tour not found with ID: " + nonExistentId)));
    }

    @Test
    void testUpdateTour_ValidationError() throws Exception {
        TourRequestDto invalidUpdate = TourRequestDto.builder()
                .name("")  // Invalid empty name
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .build();

        mockMvc.perform(put("/api/tours/{id}", testTour.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name", notNullValue()));
    }

    // ==================== DELETE TOUR TESTS ====================

    @Test
    void testDeleteTour_Success() throws Exception {
        mockMvc.perform(delete("/api/tours/{id}", testTour.getId()))
                .andExpect(status().isNoContent());

        // Verify tour is deleted
        mockMvc.perform(get("/api/tours/{id}", testTour.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTour_NotFound() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(delete("/api/tours/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Tour not found with ID: " + nonExistentId)));
    }

    // ==================== SEARCH TOURS TESTS ====================

    @Test
    void testSearchTours_Success() throws Exception {
        mockMvc.perform(get("/api/tours/search")
                        .param("q", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Vienna to Salzburg")));
    }

    @Test
    void testSearchTours_NoResults() throws Exception {
        mockMvc.perform(get("/api/tours/search")
                        .param("q", "NonExistentPlace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchTours_MultipleResults() throws Exception {
        // Add another tour with Vienna
        Tour anotherTour = new Tour();
        anotherTour.setName("Vienna to Graz");
        anotherTour.setFromLocation("Vienna");
        anotherTour.setToLocation("Graz");
        anotherTour.setTransportType("Car");
        anotherTour.setDistance(200.0);
        anotherTour.setEstimatedTime(120);
        tourRepository.save(anotherTour);

        mockMvc.perform(get("/api/tours/search")
                        .param("q", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Vienna to Salzburg", "Vienna to Graz")));
    }


    // ==================== GET TOURS BY TRANSPORT TYPE TESTS ====================

    @Test
    void testGetToursByTransportType_Success() throws Exception {
        mockMvc.perform(get("/api/tours/transport/{transportType}", "Car"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transportType", is("Car")));
    }

    @Test
    void testGetToursByTransportType_NoResults() throws Exception {
        mockMvc.perform(get("/api/tours/transport/{transportType}", "Airplane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET TOURS BY LOCATION TESTS ====================

    @Test
    void testGetToursByLocation_Success() throws Exception {
        mockMvc.perform(get("/api/tours/location")
                        .param("location", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fromLocation", is("Vienna")));
    }

    @Test
    void testGetToursByLocation_NoResults() throws Exception {
        mockMvc.perform(get("/api/tours/location")
                        .param("location", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET TOUR STATISTICS TESTS ====================

    @Test
    void testGetTourStatistics_Success() throws Exception {
        mockMvc.perform(get("/api/tours/{id}/statistics", testTour.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tourId", is(testTour.getId().intValue())))
                .andExpect(jsonPath("$.tourName", is("Vienna to Salzburg")))
                .andExpect(jsonPath("$.totalLogs", is(0)))
                .andExpect(jsonPath("$.popularity", is(0)))
                .andExpect(jsonPath("$.childFriendliness", notNullValue()))
                .andExpect(jsonPath("$.averageDistance", is(0.0)))
                .andExpect(jsonPath("$.averageTime", is(0.0)))
                .andExpect(jsonPath("$.averageRating", is(0.0)))
                .andExpect(jsonPath("$.averageDifficulty", is(0.0)));
    }

    @Test
    void testGetTourStatistics_NotFound() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/tours/{id}/statistics", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Tour not found with ID: " + nonExistentId)));
    }


    // ==================== CONCURRENT ACCESS TESTS ====================

    @Test
    void testConcurrentTourCreation() throws Exception {
        // This test simulates concurrent requests
        TourRequestDto tour1 = TourRequestDto.builder()
                .name("Concurrent Tour 1")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .build();

        TourRequestDto tour2 = TourRequestDto.builder()
                .name("Concurrent Tour 2")
                .fromLocation("Graz")
                .toLocation("Linz")
                .transportType("Bicycle")
                .build();

        // Both requests should succeed
        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tour1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tour2)))
                .andExpect(status().isCreated());

        // Verify both tours exist
        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))); // Original test tour + 2 new tours
    }
}