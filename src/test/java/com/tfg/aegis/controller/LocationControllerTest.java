package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.LocationDto;
import com.tfg.aegis.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private LocationDto locationDto;
    private static final Long LOCATION_ID = 123L;
    private static final Double LATITUDE = 41.3851;
    private static final Double LONGITUDE = 2.1734;
    private static final String LOCATION_NAME = "Barcelona";

    @BeforeEach
    void setUp() {
        locationDto = new LocationDto();
        locationDto.setId(LOCATION_ID);
        locationDto.setLatitude(LATITUDE);
        locationDto.setLongitude(LONGITUDE);
        locationDto.setName(LOCATION_NAME);
        locationDto.setTimestamp(LocalDateTime.now());
    }
    
    @Test
    void getLocation_success() {
        when(locationService.getLocation(LOCATION_ID)).thenReturn(locationDto);

        ResponseEntity<LocationDto> response = locationController.getLocation(LOCATION_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(locationDto, response.getBody());
        assertEquals(LOCATION_ID, response.getBody().getId());
        assertEquals(LATITUDE, response.getBody().getLatitude());
        assertEquals(LONGITUDE, response.getBody().getLongitude());
        assertEquals(LOCATION_NAME, response.getBody().getName());
        verify(locationService).getLocation(LOCATION_ID);
    }

    @Test
    void getLocation_withDifferentId_success() {
        Long differentId = 999L;
        LocationDto differentLocation = new LocationDto();
        differentLocation.setId(differentId);
        differentLocation.setLatitude(40.4168);
        differentLocation.setLongitude(-3.7038);
        differentLocation.setName("Madrid");
        when(locationService.getLocation(differentId)).thenReturn(differentLocation);

        ResponseEntity<LocationDto> response = locationController.getLocation(differentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentId, response.getBody().getId());
        assertEquals("Madrid", response.getBody().getName());
        verify(locationService).getLocation(differentId);
    }

    @Test
    void getLocation_withNullId_success() {
        when(locationService.getLocation(null)).thenReturn(locationDto);

        ResponseEntity<LocationDto> response = locationController.getLocation(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(locationService).getLocation(null);
    }

    @Test
    void getLocation_multipleCalls_success() {
        Long id1 = 1L, id2 = 2L, id3 = 3L;
        LocationDto loc1 = new LocationDto();
        loc1.setId(id1);
        LocationDto loc2 = new LocationDto();
        loc2.setId(id2);
        LocationDto loc3 = new LocationDto();
        loc3.setId(id3);

        when(locationService.getLocation(id1)).thenReturn(loc1);
        when(locationService.getLocation(id2)).thenReturn(loc2);
        when(locationService.getLocation(id3)).thenReturn(loc3);

        ResponseEntity<LocationDto> response1 = locationController.getLocation(id1);
        ResponseEntity<LocationDto> response2 = locationController.getLocation(id2);
        ResponseEntity<LocationDto> response3 = locationController.getLocation(id3);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertEquals(id1, response1.getBody().getId());
        assertEquals(id2, response2.getBody().getId());
        assertEquals(id3, response3.getBody().getId());
        verify(locationService).getLocation(id1);
        verify(locationService).getLocation(id2);
        verify(locationService).getLocation(id3);
    }
    
    @Test
    void createLocation_success() {
        LocationDto newLocation = new LocationDto();
        newLocation.setLatitude(LATITUDE);
        newLocation.setLongitude(LONGITUDE);
        newLocation.setName("New Location");
        when(locationService.createLocation(newLocation)).thenReturn(LOCATION_ID);

        ResponseEntity<Long> response = locationController.createLocation(newLocation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LOCATION_ID, response.getBody());
        verify(locationService).createLocation(newLocation);
    }

    @Test
    void createLocation_withDifferentData_success() {
        LocationDto newLocation = new LocationDto();
        newLocation.setLatitude(51.5074);
        newLocation.setLongitude(-0.1278);
        newLocation.setName("London");
        Long newId = 777L;
        when(locationService.createLocation(newLocation)).thenReturn(newId);

        ResponseEntity<Long> response = locationController.createLocation(newLocation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(locationService).createLocation(newLocation);
    }

    @Test
    void createLocation_withMinimalData_success() {
        LocationDto minimalLocation = new LocationDto();
        minimalLocation.setLatitude(0.0);
        minimalLocation.setLongitude(0.0);
        Long newId = 1L;
        when(locationService.createLocation(minimalLocation)).thenReturn(newId);

        ResponseEntity<Long> response = locationController.createLocation(minimalLocation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(locationService).createLocation(minimalLocation);
    }

    @Test
    void createLocation_withCompleteData_success() {
        LocationDto completeLocation = new LocationDto();
        completeLocation.setLatitude(LATITUDE);
        completeLocation.setLongitude(LONGITUDE);
        completeLocation.setName("Complete Location");
        completeLocation.setTimestamp(LocalDateTime.now());
        Long newId = 456L;
        when(locationService.createLocation(completeLocation)).thenReturn(newId);

        ResponseEntity<Long> response = locationController.createLocation(completeLocation);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(locationService).createLocation(completeLocation);
    }

    @Test
    void createLocation_withNegativeCoordinates_success() {
        LocationDto location = new LocationDto();
        location.setLatitude(-33.8688);
        location.setLongitude(151.2093);
        location.setName("Sydney");
        Long newId = 888L;
        when(locationService.createLocation(location)).thenReturn(newId);

        ResponseEntity<Long> response = locationController.createLocation(location);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(locationService).createLocation(location);
    }

    @Test
    void createLocation_withHighPrecisionCoordinates_success() {
        LocationDto location = new LocationDto();
        location.setLatitude(41.38510123456789);
        location.setLongitude(2.17340987654321);
        location.setName("Precise Location");
        Long newId = 333L;
        when(locationService.createLocation(location)).thenReturn(newId);

        ResponseEntity<Long> response = locationController.createLocation(location);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(locationService).createLocation(location);
    }

    @Test
    void createLocation_multipleLocations_success() {
        LocationDto loc1 = new LocationDto();
        loc1.setName("Location 1");
        LocationDto loc2 = new LocationDto();
        loc2.setName("Location 2");
        LocationDto loc3 = new LocationDto();
        loc3.setName("Location 3");

        when(locationService.createLocation(loc1)).thenReturn(1L);
        when(locationService.createLocation(loc2)).thenReturn(2L);
        when(locationService.createLocation(loc3)).thenReturn(3L);

        ResponseEntity<Long> response1 = locationController.createLocation(loc1);
        ResponseEntity<Long> response2 = locationController.createLocation(loc2);
        ResponseEntity<Long> response3 = locationController.createLocation(loc3);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertEquals(1L, response1.getBody());
        assertEquals(2L, response2.getBody());
        assertEquals(3L, response3.getBody());
        verify(locationService, times(3)).createLocation(any(LocationDto.class));
    }

    @Test
    void deleteLocation_success() {
        doNothing().when(locationService).deleteLocation(locationDto);

        ResponseEntity<Void> response = locationController.deleteLocation(locationDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(locationService).deleteLocation(locationDto);
    }

    @Test
    void deleteLocation_withDifferentLocation_success() {
        LocationDto differentLocation = new LocationDto();
        differentLocation.setId(555L);
        differentLocation.setName("To Delete");
        doNothing().when(locationService).deleteLocation(differentLocation);

        ResponseEntity<Void> response = locationController.deleteLocation(differentLocation);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(locationService).deleteLocation(differentLocation);
    }

    @Test
    void deleteLocation_withMinimalData_success() {
        LocationDto minimalLocation = new LocationDto();
        minimalLocation.setId(1L);
        doNothing().when(locationService).deleteLocation(minimalLocation);

        ResponseEntity<Void> response = locationController.deleteLocation(minimalLocation);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(locationService).deleteLocation(minimalLocation);
    }

    @Test
    void deleteLocation_withCompleteData_success() {
        LocationDto completeLocation = new LocationDto();
        completeLocation.setId(100L);
        completeLocation.setLatitude(LATITUDE);
        completeLocation.setLongitude(LONGITUDE);
        completeLocation.setName("Complete Location");
        completeLocation.setTimestamp(LocalDateTime.now());
        doNothing().when(locationService).deleteLocation(completeLocation);

        ResponseEntity<Void> response = locationController.deleteLocation(completeLocation);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(locationService).deleteLocation(completeLocation);
    }

    @Test
    void deleteLocation_multipleLocations_success() {
        LocationDto loc1 = new LocationDto();
        loc1.setId(10L);
        LocationDto loc2 = new LocationDto();
        loc2.setId(20L);
        LocationDto loc3 = new LocationDto();
        loc3.setId(30L);

        doNothing().when(locationService).deleteLocation(any(LocationDto.class));

        ResponseEntity<Void> response1 = locationController.deleteLocation(loc1);
        ResponseEntity<Void> response2 = locationController.deleteLocation(loc2);
        ResponseEntity<Void> response3 = locationController.deleteLocation(loc3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(locationService).deleteLocation(loc1);
        verify(locationService).deleteLocation(loc2);
        verify(locationService).deleteLocation(loc3);
    }
    
    @Test
    void createGetDelete_workflow_success() {
        LocationDto createDto = new LocationDto();
        createDto.setLatitude(LATITUDE);
        createDto.setLongitude(LONGITUDE);
        createDto.setName("Workflow Location");

        LocationDto createdDto = new LocationDto();
        createdDto.setId(999L);
        createdDto.setLatitude(LATITUDE);
        createdDto.setLongitude(LONGITUDE);
        createdDto.setName("Workflow Location");

        when(locationService.createLocation(createDto)).thenReturn(999L);
        when(locationService.getLocation(999L)).thenReturn(createdDto);
        doNothing().when(locationService).deleteLocation(createdDto);

        ResponseEntity<Long> createResponse = locationController.createLocation(createDto);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertEquals(999L, createResponse.getBody());

        ResponseEntity<LocationDto> getResponse = locationController.getLocation(999L);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(999L, getResponse.getBody().getId());
        assertEquals("Workflow Location", getResponse.getBody().getName());

        ResponseEntity<Void> deleteResponse = locationController.deleteLocation(createdDto);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        verify(locationService).createLocation(createDto);
        verify(locationService).getLocation(999L);
        verify(locationService).deleteLocation(createdDto);
    }
}
