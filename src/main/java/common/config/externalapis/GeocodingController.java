package common.config.externalapis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@RestController
@RequestMapping("/geocode")
public class GeocodingController {
    @Value("${google.places.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeocodingController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(GeocodingController.class);

    @GetMapping("/reverse")
    public ResponseEntity<String> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        try {
            // 🔥 Usar Locale.US para forzar punto decimal
            String url = String.format(Locale.US,
                    "https://maps.googleapis.com/maps/api/geocode/json?latlng=%.6f,%.6f&key=%s",
                    lat, lng, apiKey
            );

            log.info("Llamando a Google Geocoding: {}", url.replace(apiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                return ResponseEntity.status(500).body("{\"error\": \"Empty response from Google API\"}");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en geocoding: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"Geocoding failed: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<String> searchGeocode(@RequestParam String query) {
        try {
            String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    query.replace(" ", "+"), apiKey);

            log.info("Llamando a Google Geocoding: {}", url.replace(apiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"No response from Google API\"}");
            }

            if (response.isEmpty() || response.contains("\"ZERO_RESULTS\"")) {
                return ResponseEntity.noContent().build(); // 204
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error en geocoding: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"Geocoding failed: " + e.getMessage() + "\"}");
        }
    }
}
