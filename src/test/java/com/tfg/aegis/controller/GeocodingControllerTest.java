package com.tfg.aegis.controller;

import com.tfg.aegis.service.GeocodingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeocodingControllerTest {

    @Mock
    private GeocodingService geocodingService;

    @InjectMocks
    private GeocodingController geocodingController;

    private static final Double BARCELONA_LAT = 41.3851;
    private static final Double BARCELONA_LNG = 2.1734;
    private static final String VALID_JSON_RESPONSE = "{\"results\":[{\"formatted_address\":\"Barcelona, Spain\"}],\"status\":\"OK\"}";
    
    @Test
    void reverseGeocode_success() {
        when(geocodingService.reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG)).thenReturn(VALID_JSON_RESPONSE);

        ResponseEntity<String> response = geocodingController.reverseGeocode(BARCELONA_LAT, BARCELONA_LNG);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALID_JSON_RESPONSE, response.getBody());
        assertTrue(response.getBody().contains("Barcelona"));
        verify(geocodingService).reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG);
    }

    @Test
    void reverseGeocode_withDifferentCoordinates_success() {
        Double lat = 40.4168;
        Double lng = -3.7038;
        String madridResponse = "{\"results\":[{\"formatted_address\":\"Madrid, Spain\"}],\"status\":\"OK\"}";
        when(geocodingService.reverseGeocodeRaw(lat, lng)).thenReturn(madridResponse);

        ResponseEntity<String> response = geocodingController.reverseGeocode(lat, lng);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(madridResponse, response.getBody());
        verify(geocodingService).reverseGeocodeRaw(lat, lng);
    }

    @Test
    void reverseGeocode_withNegativeCoordinates_success() {
        Double lat = -33.8688;
        Double lng = 151.2093;
        String sydneyResponse = "{\"results\":[{\"formatted_address\":\"Sydney, Australia\"}],\"status\":\"OK\"}";
        when(geocodingService.reverseGeocodeRaw(lat, lng)).thenReturn(sydneyResponse);

        ResponseEntity<String> response = geocodingController.reverseGeocode(lat, lng);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sydneyResponse, response.getBody());
        verify(geocodingService).reverseGeocodeRaw(lat, lng);
    }

    @Test
    void reverseGeocode_withZeroCoordinates_success() {
        Double lat = 0.0;
        Double lng = 0.0;
        String oceanResponse = "{\"results\":[{\"formatted_address\":\"Atlantic Ocean\"}],\"status\":\"OK\"}";
        when(geocodingService.reverseGeocodeRaw(lat, lng)).thenReturn(oceanResponse);

        ResponseEntity<String> response = geocodingController.reverseGeocode(lat, lng);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(oceanResponse, response.getBody());
        verify(geocodingService).reverseGeocodeRaw(lat, lng);
    }

    @Test
    void reverseGeocode_withHighPrecisionCoordinates_success() {
        Double lat = 41.38510123456789;
        Double lng = 2.17340987654321;
        when(geocodingService.reverseGeocodeRaw(lat, lng)).thenReturn(VALID_JSON_RESPONSE);

        ResponseEntity<String> response = geocodingController.reverseGeocode(lat, lng);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(VALID_JSON_RESPONSE, response.getBody());
        verify(geocodingService).reverseGeocodeRaw(lat, lng);
    }

    @Test
    void reverseGeocode_withNullResponse_returns500() {
        when(geocodingService.reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG)).thenReturn(null);

        ResponseEntity<String> response = geocodingController.reverseGeocode(BARCELONA_LAT, BARCELONA_LNG);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Empty response from Google API"));
        verify(geocodingService).reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG);
    }

    @Test
    void reverseGeocode_withEmptyResponse_returns500() {
        when(geocodingService.reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG)).thenReturn("");

        ResponseEntity<String> response = geocodingController.reverseGeocode(BARCELONA_LAT, BARCELONA_LNG);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Empty response from Google API"));
        verify(geocodingService).reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG);
    }

    @Test
    void reverseGeocode_withException_returns500() {
        when(geocodingService.reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG))
                .thenThrow(new RuntimeException("API error"));

        ResponseEntity<String> response = geocodingController.reverseGeocode(BARCELONA_LAT, BARCELONA_LNG);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Geocoding failed"));
        verify(geocodingService).reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG);
    }

    @Test
    void reverseGeocode_withServiceException_returns500() {
        when(geocodingService.reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG))
                .thenThrow(new IllegalArgumentException("Invalid coordinates"));

        ResponseEntity<String> response = geocodingController.reverseGeocode(BARCELONA_LAT, BARCELONA_LNG);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Geocoding failed"));
        verify(geocodingService).reverseGeocodeRaw(BARCELONA_LAT, BARCELONA_LNG);
    }

    @Test
    void reverseGeocode_multipleCalls_success() {
        Double lat1 = 41.3851, lng1 = 2.1734;
        Double lat2 = 40.4168, lng2 = -3.7038;
        String response1 = "{\"results\":[{\"formatted_address\":\"Barcelona\"}]}";
        String response2 = "{\"results\":[{\"formatted_address\":\"Madrid\"}]}";
        
        when(geocodingService.reverseGeocodeRaw(lat1, lng1)).thenReturn(response1);
        when(geocodingService.reverseGeocodeRaw(lat2, lng2)).thenReturn(response2);

        ResponseEntity<String> result1 = geocodingController.reverseGeocode(lat1, lng1);
        ResponseEntity<String> result2 = geocodingController.reverseGeocode(lat2, lng2);

        assertEquals(HttpStatus.OK, result1.getStatusCode());
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertTrue(result1.getBody().contains("Barcelona"));
        assertTrue(result2.getBody().contains("Madrid"));
        verify(geocodingService).reverseGeocodeRaw(lat1, lng1);
        verify(geocodingService).reverseGeocodeRaw(lat2, lng2);
    }

    @Test
    void searchGeocode_success() {
        String query = "Barcelona, Spain";
        String expectedResponse = "{\"results\":[{\"formatted_address\":\"Barcelona, Spain\",\"geometry\":{}}],\"status\":\"OK\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(expectedResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
        assertTrue(response.getBody().contains("Barcelona"));
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withDifferentQuery_success() {
        String query = "Torre Eiffel, Paris";
        String parisResponse = "{\"results\":[{\"formatted_address\":\"Eiffel Tower, Paris, France\"}],\"status\":\"OK\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(parisResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(parisResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withAddress_success() {
        String query = "Carrer de Mallorca, 401, Barcelona";
        String addressResponse = "{\"results\":[{\"formatted_address\":\"Carrer de Mallorca, 401, Barcelona\"}],\"status\":\"OK\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(addressResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withPostalCode_success() {
        String query = "08025";
        String postalCodeResponse = "{\"results\":[{\"formatted_address\":\"08025 Barcelona, Spain\"}],\"status\":\"OK\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(postalCodeResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(postalCodeResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withSpecialCharacters_success() {
        String query = "Plaça d'Espanya";
        String plazaResponse = "{\"results\":[{\"formatted_address\":\"Plaça d'Espanya, Barcelona\"}],\"status\":\"OK\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(plazaResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(plazaResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withNullResponse_returns204() {
        String query = "Nonexistent Place XYZ123";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(null);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withEmptyString_success() {
        String query = "";
        String emptyResponse = "{\"results\":[],\"status\":\"ZERO_RESULTS\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(emptyResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withException_returns500() {
        String query = "Barcelona";
        when(geocodingService.searchGeocodeRaw(query))
                .thenThrow(new RuntimeException("API connection failed"));

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Geocoding failed"));
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withServiceException_returns500() {
        String query = "Test Query";
        when(geocodingService.searchGeocodeRaw(query))
                .thenThrow(new IllegalStateException("Service unavailable"));

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Geocoding failed"));
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_multipleCalls_success() {
        String query1 = "Barcelona";
        String query2 = "Madrid";
        String response1 = "{\"results\":[{\"formatted_address\":\"Barcelona, Spain\"}]}";
        String response2 = "{\"results\":[{\"formatted_address\":\"Madrid, Spain\"}]}";
        
        when(geocodingService.searchGeocodeRaw(query1)).thenReturn(response1);
        when(geocodingService.searchGeocodeRaw(query2)).thenReturn(response2);

        ResponseEntity<String> result1 = geocodingController.searchGeocode(query1);
        ResponseEntity<String> result2 = geocodingController.searchGeocode(query2);

        assertEquals(HttpStatus.OK, result1.getStatusCode());
        assertEquals(HttpStatus.OK, result2.getStatusCode());
        assertTrue(result1.getBody().contains("Barcelona"));
        assertTrue(result2.getBody().contains("Madrid"));
        verify(geocodingService).searchGeocodeRaw(query1);
        verify(geocodingService).searchGeocodeRaw(query2);
    }

    @Test
    void searchGeocode_withLongQuery_success() {
        String query = "This is a very long query string with multiple words that could be used to search for a specific location";
        String longQueryResponse = "{\"results\":[],\"status\":\"ZERO_RESULTS\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(longQueryResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(longQueryResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }

    @Test
    void searchGeocode_withNumericQuery_success() {
        String query = "12345";
        String numericResponse = "{\"results\":[{\"formatted_address\":\"12345\"}],\"status\":\"OK\"}";
        when(geocodingService.searchGeocodeRaw(query)).thenReturn(numericResponse);

        ResponseEntity<String> response = geocodingController.searchGeocode(query);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(numericResponse, response.getBody());
        verify(geocodingService).searchGeocodeRaw(query);
    }
}
