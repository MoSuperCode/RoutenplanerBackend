package org.example.tourplannerbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.example.tourplannerbackend.entity.Tour;
import org.example.tourplannerbackend.exception.RouteCalculationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

@Service
@Slf4j
public class RouteService {

    @Value("${ors.api.key}")
    private String apiKey;

    @Value("${ors.api.url:https://api.openrouteservice.org/v2}")
    private String baseUrl;

    @Value("${osm.tile.url:https://tile.openstreetmap.org}")
    private String tileServerUrl;

    @Value("${file.basePath:./resources/images}")
    private String basePath;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RouteService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // Ensure image directory exists
        ensureImageDirectoryExists();
    }

    /**
     * Calculate route information and update tour
     * Must-Have: OpenRouteService integration
     */
    public Tour calculateRoute(Tour tour) {
        try {
            log.info("Calculating route for tour: {} from {} to {}",
                    tour.getName(), tour.getFromLocation(), tour.getToLocation());

            // First geocode the locations
            double[] startCoords = geocode(tour.getFromLocation());
            double[] endCoords = geocode(tour.getToLocation());

            if (startCoords == null || endCoords == null) {
                throw new RouteCalculationException("Failed to geocode locations");
            }

            // Get route information
            RouteInfo routeInfo = getDirections(startCoords, endCoords, tour.getTransportType());

            if (routeInfo == null) {
                throw new RouteCalculationException("Failed to calculate route");
            }

            // Update tour with calculated information
            tour.setDistance(routeInfo.getDistance());
            tour.setEstimatedTime(routeInfo.getDuration());

            // Generate and save route map image
            String imagePath = generateRouteImage(tour, startCoords, endCoords);
            if (imagePath != null) {
                tour.setRouteImagePath(imagePath);
            }

            log.info("Route calculation successful: distance={}km, time={}min",
                    routeInfo.getDistance(), routeInfo.getDuration());

            return tour;

        } catch (Exception e) {
            log.error("Error calculating route for tour: {}", e.getMessage(), e);
            throw new RouteCalculationException("Failed to calculate route: " + e.getMessage(), e);
        }
    }

    /**
     * Geocode location to coordinates
     */
    private double[] geocode(String location) {
        try {
            String url = String.format("%s/geocode/search?api_key=%s&text=%s",
                    baseUrl, apiKey, location);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());

                if (rootNode.has("features") && rootNode.get("features").size() > 0) {
                    JsonNode feature = rootNode.get("features").get(0);
                    JsonNode coordinates = feature.get("geometry").get("coordinates");

                    double longitude = coordinates.get(0).asDouble();
                    double latitude = coordinates.get(1).asDouble();

                    log.debug("Geocoded {} to coordinates: [{}, {}]", location, longitude, latitude);
                    return new double[]{longitude, latitude};
                }
            }

            log.warn("No geocoding results found for location: {}", location);
            return null;

        } catch (Exception e) {
            log.error("Error geocoding location {}: {}", location, e.getMessage());
            return null;
        }
    }

    /**
     * Get directions between two points
     */
    private RouteInfo getDirections(double[] startCoords, double[] endCoords, String transportType) {
        try {
            String profile = mapTransportTypeToProfile(transportType);
            String url = String.format("%s/directions/%s?api_key=%s&start=%.6f,%.6f&end=%.6f,%.6f",
                    baseUrl, profile, apiKey,
                    startCoords[0], startCoords[1], endCoords[0], endCoords[1]);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/geo+json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return parseDirectionsResponse(response.getBody());
            }

            log.error("Directions API returned status: {}", response.getStatusCode());
            return null;

        } catch (RestClientException e) {
            log.error("Error calling directions API: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error processing directions response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse directions API response
     */
    private RouteInfo parseDirectionsResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.has("features") && rootNode.get("features").size() > 0) {
                JsonNode route = rootNode.get("features").get(0);
                JsonNode properties = route.get("properties");

                if (properties.has("summary")) {
                    JsonNode summary = properties.get("summary");
                    double distance = summary.get("distance").asDouble() / 1000; // Convert to km
                    int duration = (int) (summary.get("duration").asDouble() / 60); // Convert to minutes

                    return new RouteInfo(distance, duration);
                }
            }

            log.error("Could not parse route summary from response");
            return null;

        } catch (Exception e) {
            log.error("Error parsing directions response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate route image using OpenStreetMap tiles
     * Must-Have: OpenStreetMap integration
     */
    private String generateRouteImage(Tour tour, double[] startCoords, double[] endCoords) {
        try {
            // Calculate center point and zoom level
            double centerLon = (startCoords[0] + endCoords[0]) / 2;
            double centerLat = (startCoords[1] + endCoords[1]) / 2;
            int zoom = calculateZoomLevel(startCoords, endCoords);

            // Generate unique filename
            String fileName = "route_" + UUID.randomUUID().toString() + ".png";
            String fullPath = basePath + File.separator + fileName;

            // Download tile from OpenStreetMap
            int xtile = (int) Math.floor((centerLon + 180) / 360 * (1 << zoom));
            int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(centerLat)) + 1 / Math.cos(Math.toRadians(centerLat))) / Math.PI) / 2 * (1 << zoom));

            String tileUrl = tileServerUrl + "/" + zoom + "/" + xtile + "/" + ytile + ".png";

            // Download and save the tile
            downloadImage(tileUrl, fullPath);

            log.info("Generated route image: {}", fileName);
            return fileName;

        } catch (Exception e) {
            log.error("Error generating route image: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Download image from URL
     */
    private void downloadImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);

        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(destinationFile)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Calculate appropriate zoom level based on distance
     */
    private int calculateZoomLevel(double[] startCoords, double[] endCoords) {
        double deltaLon = Math.abs(startCoords[0] - endCoords[0]);
        double deltaLat = Math.abs(startCoords[1] - endCoords[1]);
        double maxDelta = Math.max(deltaLon, deltaLat);

        if (maxDelta > 10) return 5;      // Very far
        if (maxDelta > 5) return 7;       // Far
        if (maxDelta > 1) return 9;       // Medium
        if (maxDelta > 0.5) return 11;    // Close
        if (maxDelta > 0.1) return 13;    // Very close
        return 15;                         // Extremely close
    }

    /**
     * Map transport type to OpenRouteService profile
     */
    private String mapTransportTypeToProfile(String transportType) {
        return switch (transportType.toLowerCase()) {
            case "car" -> "driving-car";
            case "bicycle" -> "cycling-regular";
            case "walking" -> "foot-walking";
            case "public transport" -> "driving-car"; // ORS doesn't support public transport directly
            default -> "driving-car";
        };
    }

    /**
     * Ensure image directory exists
     */
    private void ensureImageDirectoryExists() {
        File directory = new File(basePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created image directory: {}", basePath);
            } else {
                log.warn("Failed to create image directory: {}", basePath);
            }
        }
    }

    /**
     * Inner class for route information
     */
    public static class RouteInfo {
        private final double distance; // in kilometers
        private final int duration; // in minutes

        public RouteInfo(double distance, int duration) {
            this.distance = distance;
            this.duration = duration;
        }

        public double getDistance() { return distance; }
        public int getDuration() { return duration; }
    }
}