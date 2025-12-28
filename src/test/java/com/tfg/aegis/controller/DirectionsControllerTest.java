package com.tfg.aegis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectionsControllerTest {

    @InjectMocks
    private DirectionsController directionsController;

    private static final String TEST_API_KEY = "test-api-key";
    private static final double ORIGIN_LAT = 41.3851;
    private static final double ORIGIN_LNG = 2.1734;
    private static final double DEST_LAT = 41.3879;
    private static final double DEST_LNG = 2.1699;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(directionsController, "apiKey", TEST_API_KEY);
    }

    @Test
    void getDirections_withWalkingMode_success() {
        String expectedResponse = "{\"status\":\"OK\",\"routes\":[]}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("status"));
        }
    }

    @Test
    void getDirections_withDrivingMode_success() {
        String expectedResponse = "{\"status\":\"OK\",\"routes\":[{\"mode\":\"driving\"}]}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "driving");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withTransitMode_success() {
        String expectedResponse = "{\"status\":\"OK\",\"routes\":[{\"mode\":\"transit\"}]}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "transit");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withBicyclingMode_success() {
        String expectedResponse = "{\"status\":\"OK\",\"routes\":[{\"mode\":\"bicycling\"}]}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "bicycling");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withDefaultMode_success() {
        String expectedResponse = "{\"status\":\"OK\",\"routes\":[]}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Test
    void getDirections_withDifferentCoordinates_success() {
        double originLat = 40.4168;
        double originLng = -3.7038;
        double destLat = 51.5074;
        double destLng = -0.1278;
        String expectedResponse = "{\"status\":\"OK\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    originLat, originLng, destLat, destLng, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withNullResponse_returnsNoContent() {
        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(null))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Test
    void getDirections_withEmptyResponse_returnsNoContent() {
        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(""))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Test
    void getDirections_withRestClientException_returns500() {
        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenThrow(new RestClientException("API error")))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("error"));
            assertTrue(response.getBody().contains("API error"));
        }
    }

    @Test
    void getDirections_withGenericException_returns500() {
        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenThrow(new RuntimeException("Unexpected error")))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("error"));
            assertTrue(response.getBody().contains("Unexpected error"));
        }
    }

    @Test
    void getDirections_withNegativeCoordinates_success() {
        double originLat = -33.8688;
        double originLng = 151.2093;
        double destLat = -34.6037;
        double destLng = -58.3816;
        String expectedResponse = "{\"status\":\"OK\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    originLat, originLng, destLat, destLng, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withZeroCoordinates_success() {
        String expectedResponse = "{\"status\":\"OK\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    0.0, 0.0, 0.0, 0.0, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withLargeDecimalCoordinates_success() {
        double originLat = 41.38510123456789;
        double originLng = 2.17340987654321;
        double destLat = 41.38790111222333;
        double destLng = 2.16990444555666;
        String expectedResponse = "{\"status\":\"OK\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    originLat, originLng, destLat, destLng, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withSameOriginAndDestination_success() {
        String expectedResponse = "{\"status\":\"OK\",\"routes\":[]}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(expectedResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, ORIGIN_LAT, ORIGIN_LNG, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }
    }

    @Test
    void getDirections_withComplexJsonResponse_success() {
        String complexResponse = "{\"geocoded_waypoints\":[],\"routes\":[{\"bounds\":{},\"legs\":[],\"overview_polyline\":{},\"summary\":\"Test Route\"}],\"status\":\"OK\"}";

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(RestTemplate.class,
                (mock, context) -> when(mock.getForObject(anyString(), eq(String.class)))
                        .thenReturn(complexResponse))) {

            ResponseEntity<String> response = directionsController.getDirections(
                    ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG, "walking");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(complexResponse, response.getBody());
            assertTrue(response.getBody().contains("geocoded_waypoints"));
            assertTrue(response.getBody().contains("routes"));
        }
    }
}
