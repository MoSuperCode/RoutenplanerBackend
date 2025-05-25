package org.example.tourplannerbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    /**
     * Generate tour report (Must-Have: PDF generation)
     * Contains all information of a single tour and all its associated tour logs
     */
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<byte[]> generateTourReport(@PathVariable Long tourId) {
        log.info("GET /api/reports/tour/{} - Generating tour report", tourId);

        try {
            byte[] pdfBytes = reportService.generateTourReport(tourId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "tour-report-" + tourId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating tour report for tour {}: {}", tourId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate summary report (Must-Have: PDF generation)
     * Statistical analysis for all tours with average time, distance and rating
     */
    @GetMapping("/summary")
    public ResponseEntity<byte[]> generateSummaryReport() {
        log.info("GET /api/reports/summary - Generating summary report");

        try {
            byte[] pdfBytes = reportService.generateSummaryReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "tour-summary-report.pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error generating summary report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}