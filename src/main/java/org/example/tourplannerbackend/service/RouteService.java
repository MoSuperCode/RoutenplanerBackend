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

    @Value("${ors.api.key:}")
    private String apiKey;

    @Value("${ors.api.url:https://api.openrouteservice.org}")
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

        log.info("RouteService initialized");
    }

    /**
     * Calculate route information and update tour
     * Must-Have: OpenRouteService integration
     */
    public Tour calculateRoute(Tour tour) {
        try {
            log.info("Calculating route for tour: {} from {} to {}",
                    tour.getName(), tour.getFromLocation(), tour.getToLocation());

            // Validate API key
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.warn("OpenRouteService API key not configured, using mock data");
                return calculateMockRoute(tour);
            }

            // Ensure image directory exists
            ensureImageDirectoryExists();

            // First geocode the locations
            double[] startCoords = geocode(tour.getFromLocation());
            double[] endCoords = geocode(tour.getToLocation());

            if (startCoords == null || endCoords == null) {
                log.warn("Failed to geocode locations, using mock data");
                return calculateMockRoute(tour);
            }

            // Get route information
            RouteInfo routeInfo = getDirections(startCoords, endCoords, tour.getTransportType());

            if (routeInfo == null) {
                log.warn("Failed to calculate route, using mock data");
                return calculateMockRoute(tour);
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
            return calculateMockRoute(tour);
        }
    }

    /**
     * Calculate mock route data when API is not available
     */
    private Tour calculateMockRoute(Tour tour) {
        log.info("Using mock route calculation for tour: {}", tour.getName());

        // Set some reasonable default values
        if (tour.getDistance() == null || tour.getDistance() == 0) {
            tour.setDistance(100.0); // 100 km default
        }
        if (tour.getEstimatedTime() == null || tour.getEstimatedTime() == 0) {
            tour.setEstimatedTime(120); // 2 hours default
        }

        return tour;
    }

    /**
     * Geocode location to coordinates using correct Pelias endpoint
     */
    private double[] geocode(String location) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return null;
            }

            // FIXED: Correct geocoding endpoint (Pelias-based)
            String url = String.format("%s/geocode/search?api_key=%s&text=%s&size=1",
                    baseUrl, apiKey, java.net.URLEncoder.encode(location, "UTF-8"));

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

                    // Validate coordinates
                    if (longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90) {
                        log.debug("Geocoded {} to coordinates: [{}, {}]", location, longitude, latitude);
                        return new double[]{longitude, latitude};
                    } else {
                        log.warn("Invalid coordinates for location {}: [{}, {}]", location, longitude, latitude);
                        return null;
                    }
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
     * Get directions between two points using correct v2 endpoint
     */
    private RouteInfo getDirections(double[] startCoords, double[] endCoords, String transportType) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return null;
            }

            String profile = mapTransportTypeToProfile(transportType);

            // FIXED: Correct directions endpoint with POST request and proper body
            String url = String.format("%s/v2/directions/%s", baseUrl, profile);

            // Create request body using ObjectMapper to avoid formatting issues
            ObjectMapper mapper = new ObjectMapper();
            double[][] coordinates = new double[][]{startCoords, endCoords};

            String requestBody;
            try {
                requestBody = mapper.writeValueAsString(
                        java.util.Map.of("coordinates", coordinates)
                );
                log.debug("Request body: {}", requestBody);
            } catch (Exception e) {
                log.error("Error creating request body: {}", e.getMessage());
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
            headers.set("Content-Type", "application/json; charset=utf-8");
            headers.set("Authorization", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

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

            if (rootNode.has("routes") && rootNode.get("routes").size() > 0) {
                JsonNode route = rootNode.get("routes").get(0);
                JsonNode summary = route.get("summary");

                if (summary != null) {
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
            ensureImageDirectoryExists();

            // Calculate center point and zoom level
            double centerLon = (startCoords[0] + endCoords[0]) / 2;
            double centerLat = (startCoords[1] + endCoords[1]) / 2;
            int zoom = calculateZoomLevel(startCoords, endCoords);

            // Generate unique filename
            String fileName = "route_" + UUID.randomUUID().toString() + ".png";
            String fullPath = getBasePath() + File.separator + fileName;

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
     * Ensure image directory exists - with null safety
     */
    private void ensureImageDirectoryExists() {
        try {
            String path = getBasePath();
            if (path != null && !path.trim().isEmpty()) {
                File directory = new File(path);
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    if (created) {
                        log.info("Created image directory: {}", path);
                    } else {
                        log.warn("Failed to create image directory: {}", path);
                    }
                }
            } else {
                log.warn("Base path is null or empty, using default directory");
                File directory = new File("./resources/images");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            }
        } catch (Exception e) {
            log.error("Error ensuring image directory exists: {}", e.getMessage());
        }
    }

    /**
     * Get base path with null safety
     */
    private String getBasePath() {
        return (basePath != null && !basePath.trim().isEmpty()) ? basePath : "./resources/images";
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