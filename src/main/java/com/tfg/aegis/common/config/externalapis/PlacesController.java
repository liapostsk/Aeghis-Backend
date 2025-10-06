package com.tfg.aegis.common.config.externalapis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/places")
public class PlacesController {

    @Value("${google.places.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Buscar lugares cercanos
    @GetMapping("/nearby")
    public ResponseEntity<String> searchNearbyPlaces(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "1500") int radius,
            @RequestParam(required = false) String type // opcional
    ) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("maps.googleapis.com")
                .path("/maps/api/place/nearbysearch/json")
                .queryParam("location", lat + "," + lng)
                .queryParam("radius", radius)
                .queryParam("key", apiKey);

        if (type != null && !type.isBlank()) {
            uriBuilder.queryParam("type", type);
        }

        String uri = uriBuilder.toUriString();
        String googleResponse = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.ok(googleResponse);
    }

    // Buscar lugares por texto
    @GetMapping("/textsearch")
    public ResponseEntity<String> searchPlacesByText(
            @RequestParam String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "5000") int radius
    ) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("maps.googleapis.com")
                .path("/maps/api/place/textsearch/json")

                .queryParam("query", query)
                .queryParam("key", apiKey);

        if (lat != null && lng != null) {
            uriBuilder.queryParam("location", lat + "," + lng);
            uriBuilder.queryParam("radius", radius);
        }

        String uri = uriBuilder.toUriString();
        String googleResponse = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.ok(googleResponse);
    }

    // Obtener detalles de un lugar
    @GetMapping("/details")
    public ResponseEntity<String> getPlaceDetails(@RequestParam String placeId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("maps.googleapis.com")
                .path("/maps/api/place/details/json")

                .queryParam("place_id", placeId)
                .queryParam("fields", "place_id,name,formatted_address,geometry,types")
                .queryParam("key", apiKey);

        String uri = uriBuilder.toUriString();
        String googleResponse = restTemplate.getForObject(uri, String.class);

        return ResponseEntity.ok(googleResponse);
    }
}