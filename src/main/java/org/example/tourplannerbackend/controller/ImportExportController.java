package org.example.tourplannerbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.service.ImportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import-export")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ImportExportController {

    private final ImportExportService importExportService;

    /**
     * Export all tours to JSON format
     * Must-Have: Import/Export functionality
     */
    @GetMapping("/export/tours")
    public ResponseEntity<byte[]> exportTours() {
        log.info("GET /api/import-export/export/tours - Exporting all tours");

        try {
            byte[] jsonBytes = importExportService.exportToursToJson();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "tours_export.json");
            headers.setContentLength(jsonBytes.length);

            return new ResponseEntity<>(jsonBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting tours: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export specific tour with logs to JSON
     */
    @GetMapping("/export/tour/{tourId}")
    public ResponseEntity<byte[]> exportTour(@PathVariable Long tourId) {
        log.info("GET /api/import-export/export/tour/{} - Exporting single tour", tourId);

        try {
            byte[] jsonBytes = importExportService.exportTourToJson(tourId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "tour_" + tourId + "_export.json");
            headers.setContentLength(jsonBytes.length);

            return new ResponseEntity<>(jsonBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting tour {}: {}", tourId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Import tours from JSON file
     * Must-Have: Import/Export functionality
     */
    @PostMapping("/import/tours")
    public ResponseEntity<String> importTours(@RequestParam("file") MultipartFile file) {
        log.info("POST /api/import-export/import/tours - Importing tours from file: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".json")) {
            return ResponseEntity.badRequest().body("Only JSON files are supported");
        }

        try {
            int importedCount = importExportService.importToursFromJson(file);
            String message = String.format("Successfully imported %d tours", importedCount);

            log.info("Import completed: {}", message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Error importing tours: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import tours: " + e.getMessage());
        }
    }

    /**
     * Export tours to CSV format
     */
    @GetMapping("/export/tours/csv")
    public ResponseEntity<byte[]> exportToursToCSV() {
        log.info("GET /api/import-export/export/tours/csv - Exporting tours to CSV");

        try {
            byte[] csvBytes = importExportService.exportToursToCsv();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "tours_export.csv");
            headers.setContentLength(csvBytes.length);

            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error exporting tours to CSV: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}