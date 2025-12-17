package com.tfg.aegis.controller;

import com.tfg.aegis.service.ExternalContactService;
import com.tfg.aegis.model.dto.ExternalContactDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalContactControllerTest {

    @Mock
    private ExternalContactService externalContactService;

    @InjectMocks
    private ExternalContactController externalContactController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createExternalContactForCurrentUser_shouldReturnOkWithId() {
        ExternalContactDto dto = new ExternalContactDto();
        Long expectedId = 123L;

        when(externalContactService.createExternalContactForCurrentUser(dto)).thenReturn(expectedId);

        ResponseEntity<Long> response = externalContactController.createExternalContactForCurrentUser(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedId, response.getBody());
        verify(externalContactService, times(1)).createExternalContactForCurrentUser(dto);
    }

    @Test
    void editExternalContact_shouldReturnNoContent() {
        Long contactId = 5L;
        ExternalContactDto dto = new ExternalContactDto();

        ResponseEntity<Void> response = externalContactController.editExternalContact(contactId, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(externalContactService, times(1)).editExternalContact(contactId, dto);
    }

    @Test
    void deleteExternalContact_shouldReturnNoContent() {
        Long contactId = 10L;

        ResponseEntity<Void> response = externalContactController.deleteExternalContact(contactId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(externalContactService, times(1)).deleteExternalContact(contactId);
    }
}
