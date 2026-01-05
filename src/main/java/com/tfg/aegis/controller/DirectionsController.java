package com.tfg.aegis.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Directions", description = "API of Google Maps Directions services")
@RestController
@RequestMapping("/directions")
public class DirectionsController {

    @Value("${google.maps.api.key}")
    private String apiKey;

    @GetMapping
    public ResponseEntity<String> getDirections(
            @RequestParam("originLat") double originLat,
            @RequestParam("originLng") double originLng,
            @RequestParam("destLat") double destLat,
            @RequestParam("destLng") double destLng,
            @RequestParam(name = "mode", defaultValue = "walking") String mode
    ) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://maps.googleapis.com/maps/api/directions/json")
                    .queryParam("origin", originLat + "," + originLng)
                    .queryParam("destination", destLat + "," + destLng)
                    .queryParam("mode", mode)
                    .queryParam("key", apiKey)
                    .toUriString();

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error en directions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("{\"error\": \"Directions failed: " + e.getMessage() + "\"}");
        }
    }
}
