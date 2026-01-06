package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.SafeLocationDto;
import com.tfg.aegis.service.SafeLocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SafeLocationControllerTest {

    @Mock
    private SafeLocationService safeLocationService;

    @InjectMocks
    private SafeLocationController safeLocationController;

    private SafeLocationDto safeLocationDto;
    private static final Long LOCATION_ID = 123L;
    private static final String LOCATION_NAME = "Home";
    private static final String LOCATION_ADDRESS = "Carrer de Mallorca, 401, Barcelona";
    private static final Double LATITUDE = 41.3851;
    private static final Double LONGITUDE = 2.1734;

    @BeforeEach
    void setUp() {
        safeLocationDto = new SafeLocationDto();
        safeLocationDto.setId(LOCATION_ID);
        safeLocationDto.setName(LOCATION_NAME);
        safeLocationDto.setAddress(LOCATION_ADDRESS);
        safeLocationDto.setLatitude(LATITUDE);
        safeLocationDto.setLongitude(LONGITUDE);
    }

    /* =========================
     * getSafeLocationForCurrentUser
     * ========================= */
    @Test
    void getSafeLocationForCurrentUser_success() {
        when(safeLocationService.getSafeLocationForCurrentUser(LOCATION_ID)).thenReturn(safeLocationDto);

        ResponseEntity<SafeLocationDto> response = safeLocationController.getSafeLocationForCurrentUser(LOCATION_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LOCATION_ID, response.getBody().getId());
        assertEquals(LOCATION_NAME, response.getBody().getName());
        assertEquals(LOCATION_ADDRESS, response.getBody().getAddress());
        assertEquals(LATITUDE, response.getBody().getLatitude());
        assertEquals(LONGITUDE, response.getBody().getLongitude());
        verify(safeLocationService).getSafeLocationForCurrentUser(LOCATION_ID);
    }

    @Test
    void getSafeLocationForCurrentUser_withDifferentId_success() {
        Long differentId = 999L;
        SafeLocationDto differentLocation = new SafeLocationDto();
        differentLocation.setId(differentId);
        differentLocation.setName("Different Location");
        differentLocation.setAddress("Different Address");
        differentLocation.setLatitude(40.4168);
        differentLocation.setLongitude(-3.7038);

        when(safeLocationService.getSafeLocationForCurrentUser(differentId)).thenReturn(differentLocation);

        ResponseEntity<SafeLocationDto> response = safeLocationController.getSafeLocationForCurrentUser(differentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentId, response.getBody().getId());
        assertEquals("Different Location", response.getBody().getName());
        verify(safeLocationService).getSafeLocationForCurrentUser(differentId);
    }

    @Test
    void getSafeLocationForCurrentUser_withMinimalData_success() {
        Long locationId = 456L;
        SafeLocationDto minimalLocation = new SafeLocationDto();
        minimalLocation.setId(locationId);
        minimalLocation.setLatitude(41.0);
        minimalLocation.setLongitude(2.0);

        when(safeLocationService.getSafeLocationForCurrentUser(locationId)).thenReturn(minimalLocation);

        ResponseEntity<SafeLocationDto> response = safeLocationController.getSafeLocationForCurrentUser(locationId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(locationId, response.getBody().getId());
        verify(safeLocationService).getSafeLocationForCurrentUser(locationId);
    }

    @Test
    void getSafeLocationForCurrentUser_withCompleteData_success() {
        SafeLocationDto completeLocation = new SafeLocationDto();
        completeLocation.setId(LOCATION_ID);
        completeLocation.setName("Complete Location");
        completeLocation.setAddress("Complete Address");
        completeLocation.setDescription("Complete Description");
        completeLocation.setLatitude(LATITUDE);
        completeLocation.setLongitude(LONGITUDE);
        completeLocation.setType("Work");

        when(safeLocationService.getSafeLocationForCurrentUser(LOCATION_ID)).thenReturn(completeLocation);

        ResponseEntity<SafeLocationDto> response = safeLocationController.getSafeLocationForCurrentUser(LOCATION_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SafeLocationDto body = response.getBody();
        assertNotNull(body);
        assertEquals(LOCATION_ID, body.getId());
        assertEquals("Complete Location", body.getName());
        assertEquals("Complete Address", body.getAddress());
        assertEquals("Complete Description", body.getDescription());
        assertEquals("Work", body.getType());
        verify(safeLocationService).getSafeLocationForCurrentUser(LOCATION_ID);
    }

    @Test
    void getSafeLocationForCurrentUser_multipleRequests_success() {
        Long id1 = 100L, id2 = 200L;
        SafeLocationDto location1 = new SafeLocationDto();
        location1.setId(id1);
        location1.setName("Location 1");
        SafeLocationDto location2 = new SafeLocationDto();
        location2.setId(id2);
        location2.setName("Location 2");

        when(safeLocationService.getSafeLocationForCurrentUser(id1)).thenReturn(location1);
        when(safeLocationService.getSafeLocationForCurrentUser(id2)).thenReturn(location2);

        ResponseEntity<SafeLocationDto> response1 = safeLocationController.getSafeLocationForCurrentUser(id1);
        ResponseEntity<SafeLocationDto> response2 = safeLocationController.getSafeLocationForCurrentUser(id2);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(id1, response1.getBody().getId());
        assertEquals(id2, response2.getBody().getId());
        verify(safeLocationService).getSafeLocationForCurrentUser(id1);
        verify(safeLocationService).getSafeLocationForCurrentUser(id2);
    }

    @Test
    void getSafeLocationForCurrentUser_withZeroId_success() {
        Long zeroId = 0L;
        SafeLocationDto location = new SafeLocationDto();
        location.setId(zeroId);

        when(safeLocationService.getSafeLocationForCurrentUser(zeroId)).thenReturn(location);

        ResponseEntity<SafeLocationDto> response = safeLocationController.getSafeLocationForCurrentUser(zeroId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(zeroId, response.getBody().getId());
        verify(safeLocationService).getSafeLocationForCurrentUser(zeroId);
    }

    @Test
    void getSafeLocationForCurrentUser_withMaxLongId_success() {
        Long maxId = Long.MAX_VALUE;
        SafeLocationDto location = new SafeLocationDto();
        location.setId(maxId);

        when(safeLocationService.getSafeLocationForCurrentUser(maxId)).thenReturn(location);

        ResponseEntity<SafeLocationDto> response = safeLocationController.getSafeLocationForCurrentUser(maxId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(maxId, response.getBody().getId());
        verify(safeLocationService).getSafeLocationForCurrentUser(maxId);
    }

    /* =========================
     * addSafeLocationForCurrentUser
     * ========================= */

    @Test
    void addSafeLocationForCurrentUser_success() {
        SafeLocationDto newLocation = new SafeLocationDto();
        newLocation.setName("Work");
        newLocation.setAddress("Avinguda Diagonal, 123, Barcelona");
        newLocation.setLatitude(41.3879);
        newLocation.setLongitude(2.1699);

        when(safeLocationService.addSafeLocationForCurrentUser(newLocation)).thenReturn(LOCATION_ID);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(newLocation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LOCATION_ID, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(newLocation);
    }

    @Test
    void addSafeLocationForCurrentUser_withDifferentData_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("Gym");
        location.setAddress("Carrer del Consell de Cent, 456");
        location.setLatitude(41.3900);
        location.setLongitude(2.1650);
        Long newId = 999L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void addSafeLocationForCurrentUser_withMinimalData_success() {
        SafeLocationDto minimalLocation = new SafeLocationDto();
        minimalLocation.setLatitude(LATITUDE);
        minimalLocation.setLongitude(LONGITUDE);
        Long newId = 1L;

        when(safeLocationService.addSafeLocationForCurrentUser(minimalLocation)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(minimalLocation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(minimalLocation);
    }

    @Test
    void addSafeLocationForCurrentUser_withCompleteData_success() {
        SafeLocationDto completeLocation = new SafeLocationDto();
        completeLocation.setName("Complete Location");
        completeLocation.setAddress("Full Address");
        completeLocation.setDescription("Complete description");
        completeLocation.setLatitude(LATITUDE);
        completeLocation.setLongitude(LONGITUDE);
        completeLocation.setType("Home");
        Long newId = 555L;

        when(safeLocationService.addSafeLocationForCurrentUser(completeLocation)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(completeLocation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(completeLocation);
    }

    @Test
    void editSafeLocationForCurrentUser_success() {
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Updated Name");
        editDto.setAddress("Updated Address");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_withDifferentId_success() {
        Long differentId = 777L;
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Different Location");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(differentId, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(differentId, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(differentId, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_changeName_success() {
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("New Name");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_changeAddress_success() {
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setAddress("New Address, 123");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_changeCoordinates_success() {
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setLatitude(40.4168);
        editDto.setLongitude(-3.7038);
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_changeAllFields_success() {
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Completely New Name");
        editDto.setAddress("Completely New Address");
        editDto.setDescription("New description");
        editDto.setLatitude(40.0);
        editDto.setLongitude(3.0);
        editDto.setType("Work");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_multipleEdits_success() {
        Long id1 = 10L, id2 = 20L, id3 = 30L;
        SafeLocationDto dto1 = new SafeLocationDto();
        SafeLocationDto dto2 = new SafeLocationDto();
        SafeLocationDto dto3 = new SafeLocationDto();
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(anyLong(), any(SafeLocationDto.class));

        ResponseEntity<Void> response1 = safeLocationController.editSafeLocationForCurrentUser(id1, dto1);
        ResponseEntity<Void> response2 = safeLocationController.editSafeLocationForCurrentUser(id2, dto2);
        ResponseEntity<Void> response3 = safeLocationController.editSafeLocationForCurrentUser(id3, dto3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(id1, dto1);
        verify(safeLocationService).editSafeLocationForCurrentUser(id2, dto2);
        verify(safeLocationService).editSafeLocationForCurrentUser(id3, dto3);
    }

    @Test
    void deleteSafeLocationForCurrentUser_success() {
        doNothing().when(safeLocationService).deleteSafeLocationForCurrentUser(LOCATION_ID);

        ResponseEntity<Void> response = safeLocationController.deleteSafeLocationForCurrentUser(LOCATION_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(safeLocationService).deleteSafeLocationForCurrentUser(LOCATION_ID);
    }

    @Test
    void deleteSafeLocationForCurrentUser_withDifferentId_success() {
        Long differentId = 888L;
        doNothing().when(safeLocationService).deleteSafeLocationForCurrentUser(differentId);

        ResponseEntity<Void> response = safeLocationController.deleteSafeLocationForCurrentUser(differentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).deleteSafeLocationForCurrentUser(differentId);
    }

    @Test
    void deleteSafeLocationForCurrentUser_multipleDeletes_success() {
        Long id1 = 100L, id2 = 200L, id3 = 300L;
        doNothing().when(safeLocationService).deleteSafeLocationForCurrentUser(anyLong());

        ResponseEntity<Void> response1 = safeLocationController.deleteSafeLocationForCurrentUser(id1);
        ResponseEntity<Void> response2 = safeLocationController.deleteSafeLocationForCurrentUser(id2);
        ResponseEntity<Void> response3 = safeLocationController.deleteSafeLocationForCurrentUser(id3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(safeLocationService).deleteSafeLocationForCurrentUser(id1);
        verify(safeLocationService).deleteSafeLocationForCurrentUser(id2);
        verify(safeLocationService).deleteSafeLocationForCurrentUser(id3);
    }

    @Test
    void deleteSafeLocationForCurrentUser_withZeroId_success() {
        Long zeroId = 0L;
        doNothing().when(safeLocationService).deleteSafeLocationForCurrentUser(zeroId);

        ResponseEntity<Void> response = safeLocationController.deleteSafeLocationForCurrentUser(zeroId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).deleteSafeLocationForCurrentUser(zeroId);
    }

    @Test
    void deleteSafeLocationForCurrentUser_withMaxLongId_success() {
        Long maxId = Long.MAX_VALUE;
        doNothing().when(safeLocationService).deleteSafeLocationForCurrentUser(maxId);

        ResponseEntity<Void> response = safeLocationController.deleteSafeLocationForCurrentUser(maxId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).deleteSafeLocationForCurrentUser(maxId);
    }

    @Test
    void addEditDelete_workflow_success() {
        SafeLocationDto addDto = new SafeLocationDto();
        addDto.setName("Workflow Location");
        addDto.setAddress("Workflow Address");
        addDto.setLatitude(LATITUDE);
        addDto.setLongitude(LONGITUDE);

        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Updated Workflow Location");

        when(safeLocationService.addSafeLocationForCurrentUser(addDto)).thenReturn(999L);
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(999L, editDto);
        doNothing().when(safeLocationService).deleteSafeLocationForCurrentUser(999L);

        ResponseEntity<Long> addResponse = safeLocationController.addSafeLocationForCurrentUser(addDto);

        assertEquals(HttpStatus.CREATED, addResponse.getStatusCode());
        assertEquals(999L, addResponse.getBody());

        ResponseEntity<Void> editResponse = safeLocationController.editSafeLocationForCurrentUser(999L, editDto);

        assertEquals(HttpStatus.NO_CONTENT, editResponse.getStatusCode());

        ResponseEntity<Void> deleteResponse = safeLocationController.deleteSafeLocationForCurrentUser(999L);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        verify(safeLocationService).addSafeLocationForCurrentUser(addDto);
        verify(safeLocationService).editSafeLocationForCurrentUser(999L, editDto);
        verify(safeLocationService).deleteSafeLocationForCurrentUser(999L);
    }

    /* =========================
     * Additional Edge Cases
     * ========================= */
    @Test
    void addSafeLocationForCurrentUser_withNegativeCoordinates_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("Sydney");
        location.setLatitude(-33.8688);
        location.setLongitude(151.2093);
        Long newId = 777L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void addSafeLocationForCurrentUser_withExtremeCoordinates_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("North Pole");
        location.setLatitude(90.0);
        location.setLongitude(180.0);
        Long newId = 888L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void addSafeLocationForCurrentUser_withZeroCoordinates_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("Null Island");
        location.setLatitude(0.0);
        location.setLongitude(0.0);
        Long newId = 1L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void addSafeLocationForCurrentUser_withNullName_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName(null);
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);
        Long newId = 2L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void addSafeLocationForCurrentUser_withEmptyStrings_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("");
        location.setAddress("");
        location.setDescription("");
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);
        Long newId = 3L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void editSafeLocationForCurrentUser_withNullValues_success() {
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName(null);
        editDto.setAddress(null);
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_withZeroId_success() {
        Long zeroId = 0L;
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Edited");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(zeroId, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(zeroId, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(zeroId, editDto);
    }

    @Test
    void editSafeLocationForCurrentUser_withMaxLongId_success() {
        Long maxId = Long.MAX_VALUE;
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Edited");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(maxId, editDto);

        ResponseEntity<Void> response = safeLocationController.editSafeLocationForCurrentUser(maxId, editDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(safeLocationService).editSafeLocationForCurrentUser(maxId, editDto);
    }

    @Test
    void getSafeLocationForCurrentUser_thenEdit_workflow() {
        // Get location
        when(safeLocationService.getSafeLocationForCurrentUser(LOCATION_ID)).thenReturn(safeLocationDto);

        ResponseEntity<SafeLocationDto> getResponse = safeLocationController.getSafeLocationForCurrentUser(LOCATION_ID);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        // Edit location
        SafeLocationDto editDto = new SafeLocationDto();
        editDto.setName("Edited Name");
        doNothing().when(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        ResponseEntity<Void> editResponse = safeLocationController.editSafeLocationForCurrentUser(LOCATION_ID, editDto);

        assertEquals(HttpStatus.NO_CONTENT, editResponse.getStatusCode());

        verify(safeLocationService).getSafeLocationForCurrentUser(LOCATION_ID);
        verify(safeLocationService).editSafeLocationForCurrentUser(LOCATION_ID, editDto);
    }

    @Test
    void addSafeLocationForCurrentUser_returnsCorrectId() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("Test");
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);
        Long expectedId = 12345L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(expectedId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertNotNull(response.getBody());
        assertEquals(expectedId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }

    @Test
    void addSafeLocationForCurrentUser_withLongAddress_success() {
        SafeLocationDto location = new SafeLocationDto();
        location.setName("Location with very long address");
        location.setAddress("This is a very long address with multiple lines and lots of details about the exact location including street number, building name, floor, apartment number, postal code, city, province, country and additional landmark information");
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);
        Long newId = 4L;

        when(safeLocationService.addSafeLocationForCurrentUser(location)).thenReturn(newId);

        ResponseEntity<Long> response = safeLocationController.addSafeLocationForCurrentUser(location);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(safeLocationService).addSafeLocationForCurrentUser(location);
    }
}
