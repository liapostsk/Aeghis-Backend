package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.service.EmergencyContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmergencyContactControllerTest {

    @Mock
    private EmergencyContactService emergencyContactService;

    @InjectMocks
    private EmergencyContactController emergencyContactController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addEmergencyContactForCurrentUser_shouldReturnOkWithDto() {
        EmergencyContactDto inputDto = new EmergencyContactDto();
        EmergencyContactDto expectedDto = new EmergencyContactDto();

        when(emergencyContactService.addEmergencyContactForCurrentUser(inputDto)).thenReturn(expectedDto);

        ResponseEntity<EmergencyContactDto> response =
                emergencyContactController.addEmergencyContactForCurrentUser(inputDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
        verify(emergencyContactService, times(1)).addEmergencyContactForCurrentUser(inputDto);
    }

    @Test
    void editEmergencyContact_shouldReturnNoContent() {
        Long contactId = 1L;
        EmergencyContactDto dto = new EmergencyContactDto();

        ResponseEntity<Void> response =
                emergencyContactController.editEmergencyContact(contactId, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(emergencyContactService, times(1)).editEmergencyContact(contactId, dto);
    }

    @Test
    void deleteEmergencyContact_shouldReturnNoContent() {
        Long contactId = 2L;

        ResponseEntity<Void> response =
                emergencyContactController.deleteEmergencyContact(contactId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(emergencyContactService, times(1)).deleteEmergencyContactForCurrentUser(contactId);
    }
}
