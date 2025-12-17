package com.tfg.aegis.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    @Value("${google.maps.api.key}")
    private String apiKey;

    public String reverseGeocodeRaw(Double lat, Double lng) {
        if (lat == null || lng == null) return null;

        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%.6f,%.6f&key=%s",
                lat, lng, apiKey);

        log.info("Llamando a Google Geocoding: {}", url.replace(apiKey, "***"));
        return restTemplate.getForObject(url, String.class);
    }

    public String searchGeocodeRaw(String query) {
        if (query == null || query.isBlank()) return null;

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    encoded, apiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) return null;

            // Mantengo tu lógica: si ZERO_RESULTS → 204
            if (response.contains("\"ZERO_RESULTS\"")) return null;

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Geocoding failed: " + e.getMessage(), e);
        }
    }

    public String reverseGeocodeAddress(Double lat, Double lng) {
        if (lat == null || lng == null)
            return null;

        try {
            String url = String.format(Locale.US, "https://maps.googleapis.com/maps/api/geocode/json?latlng=%.6f,%.6f&key=%s", lat, lng, apiKey);

            log.info("Llamando a Google Geocoding (address): {}", url.replace(apiKey, "***"));

            String json = restTemplate.getForObject(url, String.class);
            if (json == null || json.isBlank())
                return null;

            JsonNode root = objectMapper.readTree(json);
            JsonNode results = root.path("results");

            if (!results.isArray() || results.isEmpty())
                return null;

            JsonNode formatted = results.get(0).path("formatted_address");
            return formatted.isMissingNode() ? null : formatted.asText();

        } catch (Exception e) {
            log.warn("No se pudo resolver la dirección desde lat/lng", e);
            return null; // no romper flujo crítico (emergencia)
        }
    }
}
