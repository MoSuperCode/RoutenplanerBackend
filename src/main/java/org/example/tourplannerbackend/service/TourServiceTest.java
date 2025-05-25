package org.example.tourplannerbackend.service;

import org.example.tourplannerbackend.dto.TourRequestDto;
import org.example.tourplannerbackend.dto.TourResponseDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.exception.ResourceNotFoundException;
import org.example.tourplannerbackend.mapper.TourMapper;
import org.example.tourplannerbackend.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private RouteService routeService;

    @InjectMocks
    private TourService tourService;

    private Tour testTour;
    private TourRequestDto testTourRequestDto;
    private TourResponseDto testTourResponseDto;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setId(1L);
        testTour.setName("Test Tour");
        testTour.setFromLocation("Vienna");
        testTour.setToLocation("Salzburg");
        testTour.setTransportType("Car");
        testTour.setDistance(295.0);
        testTour.setEstimatedTime(180);

        testTourRequestDto = TourRequestDto.builder()
                .name("Test Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .distance(295.0)
                .estimatedTime(180)
                .build();

        testTourResponseDto = TourResponseDto.builder()
                .id(1L)
                .name("Test Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .distance(295.0)
                .estimatedTime(180)
                .build();
    }

    @Test
    void getTourById_ShouldReturnTour_WhenTourExists() {
        // Given
        when(tourRepository.findById(1L)).thenReturn(Optional.of(testTour));
        when(tourMapper.toResponseDto(testTour)).thenReturn(testTourResponseDto);

        // When
        TourResponseDto result = tourService.getTourById(1L);

        // Then
        assertNotNull(result);
        verify(tourLogRepository).findById(1L);
        verify(tourLogMapper).updateEntityFromDto(testTourLogRequestDto, testTourLog);
        verify(tourLogRepository).save(testTourLog);
        verify(tourLogMapper).toResponseDto(testTourLog);
    }

    @Test
    void deleteTourLog_ShouldDeleteLog_WhenLogExists() {
        // Given
        when(tourLogRepository.findById(1L)).thenReturn(Optional.of(testTourLog));

        // When
        tourLogService.deleteTourLog(1L);

        // Then
        verify(tourLogRepository).findById(1L);
        verify(tourLogRepository).delete(testTourLog);
    }
}

// Repository Tests
package org.example.tourplannerbackend.repository;

import org.example.tourplannerbackend.entity.Tour;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TourRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TourRepository tourRepository;

    @Test
    void findBySearchTerm_ShouldReturnMatchingTours() {
        // Given
        Tour tour1 = new Tour();
        tour1.setName("Vienna to Salzburg");
        tour1.setFromLocation("Vienna");
        tour1.setToLocation("Salzburg");
        tour1.setTransportType("Car");
        tour1.setDistance(295.0);
        tour1.setEstimatedTime(180);
        entityManager.persist(tour1);

        Tour tour2 = new Tour();
        tour2.setName("Graz to Linz");
        tour2.setFromLocation("Graz");
        tour2.setToLocation("Linz");
        tour2.setTransportType("Train");
        tour2.setDistance(200.0);
        tour2.setEstimatedTime(120);
        entityManager.persist(tour2);

        entityManager.flush();

        // When
        List<Tour> result = tourRepository.findBySearchTerm("Vienna");

        // Then
        assertEquals(1, result.size());
        assertEquals("Vienna to Salzburg", result.get(0).getName());
    }

    @Test
    void findByTransportTypeIgnoreCase_ShouldReturnMatchingTours() {
        // Given
        Tour tour = new Tour();
        tour.setName("Test Tour");
        tour.setFromLocation("Vienna");
        tour.setToLocation("Salzburg");
        tour.setTransportType("Car");
        tour.setDistance(295.0);
        tour.setEstimatedTime(180);
        entityManager.persist(tour);
        entityManager.flush();

        // When
        List<Tour> result = tourRepository.findByTransportTypeIgnoreCase("car");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Tour", result.get(0).getName());
    }

    @Test
    void findByDistanceBetween_ShouldReturnToursInRange() {
        // Given
        Tour tour1 = new Tour();
        tour1.setName("Short Tour");
        tour1.setFromLocation("Vienna");
        tour1.setToLocation("Salzburg");
        tour1.setTransportType("Car");
        tour1.setDistance(50.0);
        tour1.setEstimatedTime(60);
        entityManager.persist(tour1);

        Tour tour2 = new Tour();
        tour2.setName("Long Tour");
        tour2.setFromLocation("Vienna");
        tour2.setToLocation("Salzburg");
        tour2.setTransportType("Car");
        tour2.setDistance(500.0);
        tour2.setEstimatedTime(300);
        entityManager.persist(tour2);

        entityManager.flush();

        // When
        List<Tour> result = tourRepository.findByDistanceBetween(0.0, 100.0);

        // Then
        assertEquals(1, result.size());
        assertEquals("Short Tour", result.get(0).getName());
    }
}

// Controller Tests
package org.example.tourplannerbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tourplannerbackend.dto.TourRequestDto;
import org.example.tourplannerbackend.dto.TourResponseDto;
import org.example.tourplannerbackend.dto.TourSummaryDto;
import org.example.tourplannerbackend.service.TourService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourController.class)
class TourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TourService tourService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTours_ShouldReturnTourList() throws Exception {
        // Given
        List<TourSummaryDto> tours = Arrays.asList(
                TourSummaryDto.builder()
                        .id(1L)
                        .name("Test Tour")
                        .fromLocation("Vienna")
                        .toLocation("Salzburg")
                        .build()
        );
        when(tourService.getAllTours()).thenReturn(tours);

        // When & Then
        mockMvc.perform(get("/api/tours"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Tour"));

        verify(tourService).getAllTours();
    }

    @Test
    void getTourById_ShouldReturnTour_WhenTourExists() throws Exception {
        // Given
        TourResponseDto tour = TourResponseDto.builder()
                .id(1L)
                .name("Test Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .build();
        when(tourService.getTourById(1L)).thenReturn(tour);

        // When & Then
        mockMvc.perform(get("/api/tours/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Tour"))
                .andExpect(jsonPath("$.fromLocation").value("Vienna"));

        verify(tourService).getTourById(1L);
    }

    @Test
    void createTour_ShouldCreateTour_WithValidData() throws Exception {
        // Given
        TourRequestDto request = TourRequestDto.builder()
                .name("New Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .distance(295.0)
                .estimatedTime(180)
                .build();

        TourResponseDto response = TourResponseDto.builder()
                .id(1L)
                .name("New Tour")
                .fromLocation("Vienna")
                .toLocation("Salzburg")
                .transportType("Car")
                .distance(295.0)
                .estimatedTime(180)
                .build();

        when(tourService.createTour(any(TourRequestDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Tour"))
                .andExpect(jsonPath("$.fromLocation").value("Vienna"));

        verify(tourService).createTour(any(TourRequestDto.class));
    }

    @Test
    void createTour_ShouldReturnBadRequest_WithInvalidData() throws Exception {
        // Given - Invalid request without required fields
        TourRequestDto request = TourRequestDto.builder()
                .name("") // Empty name should fail validation
                .build();

        // When & Then
        mockMvc.perform(post("/api/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tourService);
    }

    @Test
    void deleteTour_ShouldDeleteTour() throws Exception {
        // Given
        doNothing().when(tourService).deleteTour(1L);

        // When & Then
        mockMvc.perform(delete("/api/tours/1"))
                .andExpect(status().isNoContent());

        verify(tourService).deleteTour(1L);
    }

    @Test
    void searchTours_ShouldReturnFilteredTours() throws Exception {
        // Given
        List<TourSummaryDto> tours = Arrays.asList(
                TourSummaryDto.builder()
                        .id(1L)
                        .name("Vienna Tour")
                        .fromLocation("Vienna")
                        .toLocation("Salzburg")
                        .build()
        );
        when(tourService.searchTours("Vienna")).thenReturn(tours);

        // When & Then
        mockMvc.perform(get("/api/tours/search")
                        .param("q", "Vienna"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Vienna Tour"));

        verify(tourService).searchTours("Vienna");
    }
}NotNull(result);
assertEquals("Test Tour", result.getName());
assertEquals("Vienna", result.getFromLocation());
assertEquals("Salzburg", result.getToLocation());
verify(tourRepository).findById(1L);
verify(tourMapper).toResponseDto(testTour);
    }

@Test
void getTourById_ShouldThrowException_WhenTourNotFound() {
    // Given
    when(tourRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> tourService.getTourById(1L));
    verify(tourRepository).findById(1L);
    verifyNoInteractions(tourMapper);
}

@Test
void createTour_ShouldCreateTour_WithValidData() {
    // Given
    when(tourMapper.toEntity(testTourRequestDto)).thenReturn(testTour);
    when(tourRepository.save(any(Tour.class))).thenReturn(testTour);
    when(tourMapper.toResponseDto(testTour)).thenReturn(testTourResponseDto);

    // When
    TourResponseDto result = tourService.createTour(testTourRequestDto);

    // Then
    assertNotNull(result);
    assertEquals("Test Tour", result.getName());
    verify(tourMapper).toEntity(testTourRequestDto);
    verify(tourRepository).save(any(Tour.class));
    verify(tourMapper).toResponseDto(testTour);
}

@Test
void updateTour_ShouldUpdateTour_WhenTourExists() {
    // Given
    when(tourRepository.findById(1L)).thenReturn(Optional.of(testTour));
    when(tourRepository.save(any(Tour.class))).thenReturn(testTour);
    when(tourMapper.toResponseDto(testTour)).thenReturn(testTourResponseDto);

    // When
    TourResponseDto result = tourService.updateTour(1L, testTourRequestDto);

    // Then
    assertNotNull(result);
    verify(tourRepository).findById(1L);
    verify(tourMapper).updateEntityFromDto(testTourRequestDto, testTour);
    verify(tourRepository).save(testTour);
    verify(tourMapper).toResponseDto(testTour);
}

@Test
void deleteTour_ShouldDeleteTour_WhenTourExists() {
    // Given
    when(tourRepository.findById(1L)).thenReturn(Optional.of(testTour));

    // When
    tourService.deleteTour(1L);

    // Then
    verify(tourRepository).findById(1L);
    verify(tourRepository).delete(testTour);
}

@Test
void searchTours_ShouldReturnFilteredTours_WhenSearchTermProvided() {
    // Given
    List<Tour> tours = Arrays.asList(testTour);
    when(tourRepository.findByComprehensiveSearch("Vienna")).thenReturn(tours);

    // When
    tourService.searchTours("Vienna");

    // Then
    verify(tourRepository).findByComprehensiveSearch("Vienna");
}

@Test
void searchTours_ShouldReturnAllTours_WhenSearchTermEmpty() {
    // Given
    List<Tour> tours = Arrays.asList(testTour);
    when(tourRepository.findAll()).thenReturn(tours);

    // When
    tourService.searchTours("");

    // Then
    verify(tourRepository).findAll();
    verify(tourRepository, never()).findByComprehensiveSearch(anyString());
}
}

// TourLog Service Test
        package org.example.tourplannerbackend.service;

import org.example.tourplannerbackend.dto.TourLogRequestDto;
import org.example.tourplannerbackend.dto.TourLogResponseDto;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.entity.TourLog;
import org.example.tourplannerbackend.exception.ResourceNotFoundException;
import org.example.tourplannerbackend.mapper.TourLogMapper;
import org.example.tourplannerbackend.repository.TourLogRepository;
import org.example.tourplannerbackend.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourLogServiceTest {

    @Mock
    private TourLogRepository tourLogRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private TourLogMapper tourLogMapper;

    @InjectMocks
    private TourLogService tourLogService;

    private Tour testTour;
    private TourLog testTourLog;
    private TourLogRequestDto testTourLogRequestDto;
    private TourLogResponseDto testTourLogResponseDto;

    @BeforeEach
    void setUp() {
        testTour = new Tour();
        testTour.setId(1L);
        testTour.setName("Test Tour");

        testTourLog = new TourLog();
        testTourLog.setId(1L);
        testTourLog.setDate(LocalDateTime.now());
        testTourLog.setComment("Test comment");
        testTourLog.setDifficulty(5);
        testTourLog.setTotalDistance(100.0);
        testTourLog.setTotalTime(60);
        testTourLog.setRating(4);
        testTourLog.setTour(testTour);

        testTourLogRequestDto = TourLogRequestDto.builder()
                .date(LocalDateTime.now())
                .comment("Test comment")
                .difficulty(5)
                .totalDistance(100.0)
                .totalTime(60)
                .rating(4)
                .build();

        testTourLogResponseDto = TourLogResponseDto.builder()
                .id(1L)
                .date(LocalDateTime.now())
                .comment("Test comment")
                .difficulty(5)
                .totalDistance(100.0)
                .totalTime(60)
                .rating(4)
                .tourId(1L)
                .build();
    }

    @Test
    void getTourLogs_ShouldReturnLogs_WhenTourExists() {
        // Given
        when(tourRepository.existsById(1L)).thenReturn(true);
        when(tourLogRepository.findByTourIdOrderByDateDesc(1L)).thenReturn(Arrays.asList(testTourLog));
        when(tourLogMapper.toResponseDto(testTourLog)).thenReturn(testTourLogResponseDto);

        // When
        List<TourLogResponseDto> result = tourLogService.getTourLogs(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tourRepository).existsById(1L);
        verify(tourLogRepository).findByTourIdOrderByDateDesc(1L);
    }

    @Test
    void getTourLogs_ShouldThrowException_WhenTourNotFound() {
        // Given
        when(tourRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> tourLogService.getTourLogs(1L));
        verify(tourRepository).existsById(1L);
        verifyNoInteractions(tourLogRepository);
    }

    @Test
    void createTourLog_ShouldCreateLog_WithValidData() {
        // Given
        when(tourRepository.findById(1L)).thenReturn(Optional.of(testTour));
        when(tourLogMapper.toEntity(testTourLogRequestDto)).thenReturn(testTourLog);
        when(tourLogRepository.save(any(TourLog.class))).thenReturn(testTourLog);
        when(tourLogMapper.toResponseDto(testTourLog)).thenReturn(testTourLogResponseDto);

        // When
        TourLogResponseDto result = tourLogService.createTourLog(1L, testTourLogRequestDto);

        // Then
        assertNotNull(result);
        assertEquals("Test comment", result.getComment());
        verify(tourRepository).findById(1L);
        verify(tourLogMapper).toEntity(testTourLogRequestDto);
        verify(tourLogRepository).save(any(TourLog.class));
    }

    @Test
    void updateTourLog_ShouldUpdateLog_WhenLogExists() {
        // Given
        when(tourLogRepository.findById(1L)).thenReturn(Optional.of(testTourLog));
        when(tourLogRepository.save(any(TourLog.class))).thenReturn(testTourLog);
        when(tourLogMapper.toResponseDto(testTourLog)).thenReturn(testTourLogResponseDto);

        // When
        TourLogResponseDto result = tourLogService.updateTourLog(1L, testTourLogRequestDto);

        // Then
        assert