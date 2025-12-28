package com.tfg.aegis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GeocodingService service;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(service, "apiKey", "test-api-key");
    }

    /* =========================
     * reverseGeocodeRaw
     * ========================= */
    @Test
    void reverseGeocodeRaw_withValidCoordinates_returnsJsonString() {
        Double lat = 41.3874;
        Double lng = 2.1686;
        String expectedResponse = "{\"results\":[{\"formatted_address\":\"Barcelona, Spain\"}]}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        String result = service.reverseGeocodeRaw(lat, lng);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restTemplate).getForObject(contains("latlng=41.387400,2.168600"), eq(String.class));
        verify(restTemplate).getForObject(contains("key=test-api-key"), eq(String.class));
    }

    @Test
    void reverseGeocodeRaw_withNullLatitude_returnsNull() {
        String result = service.reverseGeocodeRaw(null, 2.1686);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void reverseGeocodeRaw_withNullLongitude_returnsNull() {
        String result = service.reverseGeocodeRaw(41.3874, null);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void reverseGeocodeRaw_withBothNull_returnsNull() {
        String result = service.reverseGeocodeRaw(null, null);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void reverseGeocodeRaw_withZeroCoordinates_callsApi() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{\"results\":[]}");

        String result = service.reverseGeocodeRaw(0.0, 0.0);

        assertNotNull(result);
        verify(restTemplate).getForObject(contains("latlng=0.000000,0.000000"), eq(String.class));
    }

    @Test
    void reverseGeocodeRaw_withNegativeCoordinates_callsApi() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{\"results\":[]}");

        String result = service.reverseGeocodeRaw(-33.8688, -151.2093);

        assertNotNull(result);
        verify(restTemplate).getForObject(contains("latlng=-33.868800,-151.209300"), eq(String.class));
    }

    /* =========================
     * searchGeocodeRaw
     * ========================= */
    @Test
    void searchGeocodeRaw_withValidQuery_returnsJsonString() {
        String query = "Barcelona, Spain";
        String expectedResponse = "{\"results\":[{\"formatted_address\":\"Barcelona, Spain\"}],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        String result = service.searchGeocodeRaw(query);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restTemplate).getForObject(contains("address=Barcelona"), eq(String.class));
        verify(restTemplate).getForObject(contains("key=test-api-key"), eq(String.class));
    }

    @Test
    void searchGeocodeRaw_withSpecialCharacters_encodesCorrectly() {
        String query = "Carrer de la Pau, 1";
        String expectedResponse = "{\"results\":[],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(expectedResponse);

        String result = service.searchGeocodeRaw(query);

        assertNotNull(result);
        verify(restTemplate).getForObject(contains("address=Carrer"), eq(String.class));
        verify(restTemplate).getForObject(contains("key=test-api-key"), eq(String.class));
    }

    @Test
    void searchGeocodeRaw_withZeroResults_returnsNull() {
        String query = "NonExistentPlace12345";
        String zeroResultsResponse = "{\"results\":[],\"status\":\"ZERO_RESULTS\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(zeroResultsResponse);

        String result = service.searchGeocodeRaw(query);

        assertNull(result);
        verify(restTemplate).getForObject(anyString(), eq(String.class));
    }

    @Test
    void searchGeocodeRaw_withNullQuery_returnsNull() {
        String result = service.searchGeocodeRaw(null);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void searchGeocodeRaw_withBlankQuery_returnsNull() {
        String result = service.searchGeocodeRaw("   ");

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void searchGeocodeRaw_withEmptyQuery_returnsNull() {
        String result = service.searchGeocodeRaw("");

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void searchGeocodeRaw_whenApiReturnsNull_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(null);

        String result = service.searchGeocodeRaw("Barcelona");

        assertNull(result);
    }

    @Test
    void searchGeocodeRaw_whenApiReturnsBlank_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("   ");

        String result = service.searchGeocodeRaw("Barcelona");

        assertNull(result);
    }

    @Test
    void searchGeocodeRaw_whenExceptionThrown_throwsRuntimeException() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.searchGeocodeRaw("Barcelona"));

        assertTrue(ex.getMessage().contains("Geocoding failed"));
        assertTrue(ex.getMessage().contains("Network error"));
    }

    /* =========================
     * reverseGeocodeAddress
     * ========================= */
    @Test
    void reverseGeocodeAddress_withValidCoordinates_returnsFormattedAddress() {
        Double lat = 41.3874;
        Double lng = 2.1686;
        String apiResponse = "{\"results\":[{\"formatted_address\":\"Barcelona, Spain\"}],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        String result = service.reverseGeocodeAddress(lat, lng);

        assertNotNull(result);
        assertEquals("Barcelona, Spain", result);
        verify(restTemplate).getForObject(contains("latlng=41.387400,2.168600"), eq(String.class));
    }

    @Test
    void reverseGeocodeAddress_withMultipleResults_returnsFirstFormattedAddress() {
        String apiResponse = "{\"results\":[" +
                "{\"formatted_address\":\"First Address\"}," +
                "{\"formatted_address\":\"Second Address\"}" +
                "],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        String result = service.reverseGeocodeAddress(40.4168, -3.7038);

        assertEquals("First Address", result);
    }

    @Test
    void reverseGeocodeAddress_withNullLatitude_returnsNull() {
        String result = service.reverseGeocodeAddress(null, 2.1686);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void reverseGeocodeAddress_withNullLongitude_returnsNull() {
        String result = service.reverseGeocodeAddress(41.3874, null);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void reverseGeocodeAddress_withBothNull_returnsNull() {
        String result = service.reverseGeocodeAddress(null, null);

        assertNull(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void reverseGeocodeAddress_whenApiReturnsNull_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(null);

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result);
    }

    @Test
    void reverseGeocodeAddress_whenApiReturnsBlank_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("   ");

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result);
    }

    @Test
    void reverseGeocodeAddress_whenResultsArrayIsEmpty_returnsNull() {
        String apiResponse = "{\"results\":[],\"status\":\"ZERO_RESULTS\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result);
    }

    @Test
    void reverseGeocodeAddress_whenResultsNotArray_returnsNull() {
        String apiResponse = "{\"results\":\"not an array\",\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result);
    }

    @Test
    void reverseGeocodeAddress_whenFormattedAddressMissing_returnsNull() {
        String apiResponse = "{\"results\":[{\"place_id\":\"123\"}],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result);
    }

    @Test
    void reverseGeocodeAddress_whenExceptionThrown_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result); // No rompe el flujo, retorna null
    }

    @Test
    void reverseGeocodeAddress_whenInvalidJson_returnsNull() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("invalid json {{{");

        String result = service.reverseGeocodeAddress(41.3874, 2.1686);

        assertNull(result);
    }

    @Test
    void reverseGeocodeAddress_withZeroCoordinates_callsApi() {
        String apiResponse = "{\"results\":[{\"formatted_address\":\"Ocean\"}],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        String result = service.reverseGeocodeAddress(0.0, 0.0);

        assertEquals("Ocean", result);
        verify(restTemplate).getForObject(contains("latlng=0.000000,0.000000"), eq(String.class));
    }

    /* =========================
     * Integration scenarios
     * ========================= */
    @Test
    void allMethods_handleApiKeyCorrectly() {
        String apiResponse = "{\"results\":[{\"formatted_address\":\"Test\"}],\"status\":\"OK\"}";

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        service.reverseGeocodeRaw(41.0, 2.0);
        service.searchGeocodeRaw("Barcelona");
        service.reverseGeocodeAddress(41.0, 2.0);

        verify(restTemplate, times(3)).getForObject(contains("key=test-api-key"), eq(String.class));
    }
}
