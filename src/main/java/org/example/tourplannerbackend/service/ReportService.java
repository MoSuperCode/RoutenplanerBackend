package org.example.tourplannerbackend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.entity.TourLog;
import org.example.tourplannerbackend.exception.ResourceNotFoundException;
import org.example.tourplannerbackend.repository.TourRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TourRepository tourRepository;

    @Value("${file.basePath:./resources/images}")
    private String basePath;

    // Must-Have: PDF Report Generation

    /**
     * Generate tour report - contains all information of a single tour and all its associated tour logs
     */
    public byte[] generateTourReport(Long tourId) {
        log.info("Generating tour report for tour ID: {}", tourId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with ID: " + tourId));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("Tour Report: " + tour.getName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Add tour details
            addTourDetails(document, tour);

            // Add tour statistics
            addTourStatistics(document, tour);

            // Add tour logs
            addTourLogs(document, tour);

            document.close();

            log.info("Tour report generated successfully for tour: {}", tour.getName());
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating tour report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate tour report", e);
        }
    }

    /**
     * Generate summary report - statistical analysis for all tours
     */
    public byte[] generateSummaryReport() {
        log.info("Generating summary report for all tours");

        List<Tour> tours = tourRepository.findAll();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("Tour Summary Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), dateFont);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Add overall statistics
            addOverallStatistics(document, tours);

            // Add summary table for all tours
            addTourSummaryTable(document, tours);

            document.close();

            log.info("Summary report generated successfully for {} tours", tours.size());
            return baos.toByteArray();

        } catch (DocumentException e) {
            log.error("Error generating summary report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate summary report", e);
        }
    }

    /**
     * Add tour details section to document
     */
    private void addTourDetails(Document document, Tour tour) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph section = new Paragraph("Tour Details", sectionFont);
        section.setSpacingBefore(20);
        section.setSpacingAfter(10);
        document.add(section);

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        // Create details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableRow(table, "Name:", tour.getName());
        addTableRow(table, "From:", tour.getFromLocation());
        addTableRow(table, "To:", tour.getToLocation());
        addTableRow(table, "Transport Type:", tour.getTransportType());
        addTableRow(table, "Distance:", String.format("%.2f km", tour.getDistance()));
        addTableRow(table, "Estimated Time:", String.format("%d minutes", tour.getEstimatedTime()));
        addTableRow(table, "Description:", tour.getDescription() != null ? tour.getDescription() : "No description");
        addTableRow(table, "Created:", tour.getCreatedAt() != null ?
                tour.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A");

        document.add(table);
    }

    /**
     * Add tour statistics section to document
     */
    private void addTourStatistics(Document document, Tour tour) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph section = new Paragraph("Tour Statistics", sectionFont);
        section.setSpacingBefore(20);
        section.setSpacingAfter(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addTableRow(table, "Total Logs:", String.valueOf(tour.getTourLogs().size()));
        addTableRow(table, "Popularity:", String.valueOf(tour.getPopularity()));
        addTableRow(table, "Child-Friendliness:", String.format("%.2f/10", tour.getChildFriendliness()));

        if (!tour.getTourLogs().isEmpty()) {
            double avgDistance = tour.getTourLogs().stream().mapToDouble(TourLog::getTotalDistance).average().orElse(0);
            double avgTime = tour.getTourLogs().stream().mapToDouble(TourLog::getTotalTime).average().orElse(0);
            double avgRating = tour.getTourLogs().stream().mapToDouble(TourLog::getRating).average().orElse(0);
            double avgDifficulty = tour.getTourLogs().stream().mapToDouble(TourLog::getDifficulty).average().orElse(0);

            addTableRow(table, "Average Distance:", String.format("%.2f km", avgDistance));
            addTableRow(table, "Average Time:", String.format("%.0f minutes", avgTime));
            addTableRow(table, "Average Rating:", String.format("%.2f/5", avgRating));
            addTableRow(table, "Average Difficulty:", String.format("%.2f/10", avgDifficulty));
        }

        document.add(table);
    }

    /**
     * Add tour logs section to document
     */
    private void addTourLogs(Document document, Tour tour) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph section = new Paragraph("Tour Logs", sectionFont);
        section.setSpacingBefore(20);
        section.setSpacingAfter(10);
        document.add(section);

        if (tour.getTourLogs().isEmpty()) {
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.GRAY);
            Paragraph noLogs = new Paragraph("No tour logs available.", normalFont);
            document.add(noLogs);
            return;
        }

        // Create logs table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1.5f, 1, 1, 1, 3});

        // Add header
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        addHeaderCell(table, "Date", headerFont);
        addHeaderCell(table, "Distance (km)", headerFont);
        addHeaderCell(table, "Time (min)", headerFont);
        addHeaderCell(table, "Difficulty", headerFont);
        addHeaderCell(table, "Rating", headerFont);
        addHeaderCell(table, "Comment", headerFont);

        // Add data rows
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        for (TourLog log : tour.getTourLogs()) {
            addDataCell(table, log.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), cellFont);
            addDataCell(table, String.format("%.2f", log.getTotalDistance()), cellFont);
            addDataCell(table, String.valueOf(log.getTotalTime()), cellFont);
            addDataCell(table, String.valueOf(log.getDifficulty()), cellFont);
            addDataCell(table, String.valueOf(log.getRating()), cellFont);
            addDataCell(table, log.getComment() != null ? log.getComment() : "", cellFont);
        }

        document.add(table);
    }

    /**
     * Add overall statistics for summary report
     */
    private void addOverallStatistics(Document document, List<Tour> tours) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph section = new Paragraph("Overall Statistics", sectionFont);
        section.setSpacingBefore(20);
        section.setSpacingAfter(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        int totalTours = tours.size();
        int totalLogs = tours.stream().mapToInt(tour -> tour.getTourLogs().size()).sum();
        double avgDistance = tours.stream().mapToDouble(Tour::getDistance).average().orElse(0);
        double avgTime = tours.stream().mapToDouble(Tour::getEstimatedTime).average().orElse(0);

        addTableRow(table, "Total Tours:", String.valueOf(totalTours));
        addTableRow(table, "Total Tour Logs:", String.valueOf(totalLogs));
        addTableRow(table, "Average Tour Distance:", String.format("%.2f km", avgDistance));
        addTableRow(table, "Average Estimated Time:", String.format("%.0f minutes", avgTime));

        document.add(table);
    }

    /**
     * Add tour summary table for summary report
     */
    private void addTourSummaryTable(Document document, List<Tour> tours) throws DocumentException {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
        Paragraph section = new Paragraph("Tour Summary", sectionFont);
        section.setSpacingBefore(20);
        section.setSpacingAfter(10);
        document.add(section);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2, 1.5f, 1.5f, 1, 1.5f});

        // Add header
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        addHeaderCell(table, "Tour Name", headerFont);
        addHeaderCell(table, "From", headerFont);
        addHeaderCell(table, "To", headerFont);
        addHeaderCell(table, "Distance", headerFont);
        addHeaderCell(table, "Time", headerFont);
        addHeaderCell(table, "Logs", headerFont);
        addHeaderCell(table, "Avg Rating", headerFont);

        // Add data rows
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        for (Tour tour : tours) {
            addDataCell(table, tour.getName(), cellFont);
            addDataCell(table, tour.getFromLocation(), cellFont);
            addDataCell(table, tour.getToLocation(), cellFont);
            addDataCell(table, String.format("%.1f km", tour.getDistance()), cellFont);
            addDataCell(table, String.format("%d min", tour.getEstimatedTime()), cellFont);
            addDataCell(table, String.valueOf(tour.getTourLogs().size()), cellFont);

            double avgRating = tour.getTourLogs().stream()
                    .mapToDouble(TourLog::getRating)
                    .average()
                    .orElse(0);
            addDataCell(table, String.format("%.1f", avgRating), cellFont);
        }

        document.add(table);
    }

    // Helper methods for table creation
    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
}