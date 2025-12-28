package com.tfg.aegis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlacesControllerTest {

    @Mock
    private RestTemplate restTemplate;

    private PlacesController placesController;

    private static final String TEST_API_KEY = "test-api-key";
    private static final double BARCELONA_LAT = 41.3851;
    private static final double BARCELONA_LNG = 2.1734;
    private static final String VALID_PLACE_ID = "ChIJ5TCOcRaYpBIRCmZHTz37sEQ";

    @BeforeEach
    void setUp() {
        placesController = new PlacesController();
        ReflectionTestUtils.setField(placesController, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(placesController, "restTemplate", restTemplate);
    }

    // ============ SEARCH NEARBY PLACES TESTS ============

    @Test
    void searchNearbyPlaces_withDefaultRadius_success() {
        // Given
        String expectedResponse = "{\"results\":[{\"name\":\"Test Place\"}],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                BARCELONA_LAT, BARCELONA_LNG, 1500, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertNotNull(response.getBody());
    }

    @Test
    void searchNearbyPlaces_withCustomRadius_success() {
        // Given
        int customRadius = 3000;
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                BARCELONA_LAT, BARCELONA_LNG, customRadius, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void searchNearbyPlaces_withType_success() {
        // Given
        String type = "restaurant";
        String expectedResponse = "{\"results\":[{\"name\":\"Restaurant\",\"types\":[\"restaurant\"]}],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                BARCELONA_LAT, BARCELONA_LNG, 1500, type);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("restaurant"));
    }

    @Test
    void searchNearbyPlaces_withDifferentTypes_success() {
        // Given
        String[] types = {"cafe", "hospital", "park", "museum", "pharmacy"};

        for (String type : types) {
            String expectedResponse = "{\"results\":[{\"types\":[\"" + type + "\"]}],\"status\":\"OK\"}";
            when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

            // When
            ResponseEntity<String> response = placesController.searchNearbyPlaces(
                    BARCELONA_LAT, BARCELONA_LNG, 1500, type);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains(type));
        }
    }

    @Test
    void searchNearbyPlaces_withDifferentCoordinates_success() {
        // Given
        double madridLat = 40.4168;
        double madridLng = -3.7038;
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                madridLat, madridLng, 1500, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void searchNearbyPlaces_withSmallRadius_success() {
        // Given
        int smallRadius = 100;
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                BARCELONA_LAT, BARCELONA_LNG, smallRadius, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void searchNearbyPlaces_withLargeRadius_success() {
        // Given
        int largeRadius = 50000;
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                BARCELONA_LAT, BARCELONA_LNG, largeRadius, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void searchNearbyPlaces_withBlankType_success() {
        // Given
        String blankType = "   ";
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchNearbyPlaces(
                BARCELONA_LAT, BARCELONA_LNG, 1500, blankType);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ============ SEARCH PLACES BY TEXT TESTS ============

    @Test
    void searchPlacesByText_withQueryOnly_success() {
        // Given
        String query = "restaurants in Barcelona";
        String expectedResponse = "{\"results\":[{\"name\":\"Restaurant 1\"}],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchPlacesByText(
                query, null, null, 5000);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void searchPlacesByText_withLocationAndRadius_success() {
        // Given
        String query = "coffee shop";
        String expectedResponse = "{\"results\":[{\"name\":\"Coffee Shop\"}],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchPlacesByText(
                query, BARCELONA_LAT, BARCELONA_LNG, 2000);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void searchPlacesByText_withCustomRadius_success() {
        // Given
        String query = "pharmacy";
        int customRadius = 10000;
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchPlacesByText(
                query, BARCELONA_LAT, BARCELONA_LNG, customRadius);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void searchPlacesByText_withDifferentQueries_success() {
        // Given
        String[] queries = {"pizza", "hotel", "gym", "supermarket", "gas station"};

        for (String query : queries) {
            String expectedResponse = "{\"results\":[{\"name\":\"" + query + "\"}],\"status\":\"OK\"}";
            when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

            // When
            ResponseEntity<String> response = placesController.searchPlacesByText(
                    query, null, null, 5000);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains(query));
        }
    }

    @Test
    void searchPlacesByText_withOnlyLatitude_success() {
        // Given
        String query = "test query";
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchPlacesByText(
                query, BARCELONA_LAT, null, 5000);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void searchPlacesByText_withOnlyLongitude_success() {
        // Given
        String query = "test query";
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.searchPlacesByText(
                query, null, BARCELONA_LNG, 5000);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ============ GET PLACE DETAILS TESTS ============

    @Test
    void getPlaceDetails_success() {
        // Given
        String expectedResponse = "{\"result\":{\"place_id\":\"" + VALID_PLACE_ID + "\",\"name\":\"Barcelona\"},\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.getPlaceDetails(VALID_PLACE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(VALID_PLACE_ID));
        assertTrue(response.getBody().contains("Barcelona"));
    }

    @Test
    void getPlaceDetails_withDifferentPlaceId_success() {
        // Given
        String differentPlaceId = "ChIJN1t_tDeuEmsRUsoyG83frY4";
        String expectedResponse = "{\"result\":{\"place_id\":\"" + differentPlaceId + "\",\"name\":\"Sydney Opera House\"},\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<String> response = placesController.getPlaceDetails(differentPlaceId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(differentPlaceId));
    }

    @Test
    void getPlaceDetails_withComplexResponse_success() {
        // Given
        String complexResponse = "{\"result\":{\"place_id\":\"test\",\"name\":\"Test Place\",\"formatted_address\":\"Test Address\",\"geometry\":{\"location\":{\"lat\":41.3851,\"lng\":2.1734}},\"types\":[\"point_of_interest\"]},\"status\":\"OK\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(complexResponse);

        // When
        ResponseEntity<String> response = placesController.getPlaceDetails(VALID_PLACE_ID);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("geometry"));
        assertTrue(response.getBody().contains("formatted_address"));
        assertTrue(response.getBody().contains("types"));
    }

    @Test
    void getPlaceDetails_multipleCalls_success() {
        // Given
        String placeId1 = "place1";
        String placeId2 = "place2";
        String placeId3 = "place3";
        String response1 = "{\"result\":{\"place_id\":\"place1\"},\"status\":\"OK\"}";
        String response2 = "{\"result\":{\"place_id\":\"place2\"},\"status\":\"OK\"}";
        String response3 = "{\"result\":{\"place_id\":\"place3\"},\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(response1)
                .thenReturn(response2)
                .thenReturn(response3);

        // When
        ResponseEntity<String> result1 = placesController.getPlaceDetails(placeId1);
        ResponseEntity<String> result2 = placesController.getPlaceDetails(placeId2);
        ResponseEntity<String> result3 = placesController.getPlaceDetails(placeId3);

        // Then
        assertEquals(HttpStatus.OK, result1.getStatusCode());
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertEquals(HttpStatus.OK, result3.getStatusCode());
        assertNotNull(result1.getBody());
        assertNotNull(result2.getBody());
        assertNotNull(result3.getBody());
        assertTrue(result1.getBody().contains("place1"));
        assertTrue(result2.getBody().contains("place2"));
        assertTrue(result3.getBody().contains("place3"));
    }
}
