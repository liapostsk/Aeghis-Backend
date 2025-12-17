package com.tfg.aegis.controller;

import com.tfg.aegis.service.GeocodingService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/geocode")
@AllArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    private static final Logger log = LoggerFactory.getLogger(GeocodingController.class);

    @GetMapping("/reverse")
    public ResponseEntity<String> reverseGeocode(@RequestParam Double lat, @RequestParam Double lng) {
        try {
            String response = geocodingService.reverseGeocodeRaw(lat, lng);

            if (response == null || response.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Empty response from Google API\"}");
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en geocoding:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Geocoding failed\"}");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchGeocode(@RequestParam String query) {
        try {
            String response = geocodingService.searchGeocodeRaw(query);

            if (response == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en geocoding search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Geocoding failed\"}");
        }
    }
}
