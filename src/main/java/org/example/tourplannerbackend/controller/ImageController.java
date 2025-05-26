package org.example.tourplannerbackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images")
@Slf4j
@CrossOrigin(origins = "*")
public class ImageController {

    @Value("${file.basePath:./resources/images}")
    private String basePath;

    /**
     * Serve tour route images
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            log.info("GET /api/images/{} - Serving image", fileName);

            // Security check: prevent directory traversal
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                log.warn("Invalid file name requested: {}", fileName);
                return ResponseEntity.badRequest().build();
            }

            Path imagePath = Paths.get(basePath).resolve(fileName);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                MediaType contentType = MediaType.IMAGE_PNG; // default
                if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = MediaType.IMAGE_JPEG;
                }

                log.info("Image served successfully: {}", fileName);
                return ResponseEntity.ok()
                        .contentType(contentType)
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // 1 hour cache
                        .body(resource);
            } else {
                log.warn("Image not found: {}", fileName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error serving image {}: {}", fileName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}