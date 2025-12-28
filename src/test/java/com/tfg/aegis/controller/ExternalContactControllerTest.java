package com.tfg.aegis.controller;

import com.tfg.aegis.service.ExternalContactService;
import com.tfg.aegis.model.dto.ExternalContactDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalContactControllerTest {

    @Mock
    private ExternalContactService externalContactService;

    @InjectMocks
    private ExternalContactController externalContactController;

    private ExternalContactDto externalContactDto;
    private static final Long CONTACT_ID = 123L;
    private static final String PHONE = "+34666777888";
    private static final String NAME = "John Doe";
    private static final String RELATION = "Friend";

    @BeforeEach
    void setUp() {
        externalContactDto = new ExternalContactDto();
        externalContactDto.setId(CONTACT_ID);
        externalContactDto.setPhone(PHONE);
        externalContactDto.setName(NAME);
        externalContactDto.setRelation(RELATION);
    }

    @Test
    void createExternalContactForCurrentUser_shouldReturnOkWithId() {
        when(externalContactService.createExternalContactForCurrentUser(externalContactDto)).thenReturn(CONTACT_ID);

        ResponseEntity<Long> response = externalContactController.createExternalContactForCurrentUser(externalContactDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(CONTACT_ID, response.getBody());
        verify(externalContactService, times(1)).createExternalContactForCurrentUser(externalContactDto);
    }

    @Test
    void createExternalContactForCurrentUser_withDifferentData_success() {
        ExternalContactDto newContact = new ExternalContactDto();
        newContact.setPhone("+1234567890");
        newContact.setName("Jane Smith");
        newContact.setRelation("Sister");
        Long newId = 999L;
        when(externalContactService.createExternalContactForCurrentUser(newContact)).thenReturn(newId);

        ResponseEntity<Long> response = externalContactController.createExternalContactForCurrentUser(newContact);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(externalContactService).createExternalContactForCurrentUser(newContact);
    }

    @Test
    void createExternalContactForCurrentUser_withMinimalData_success() {
        ExternalContactDto minimalContact = new ExternalContactDto();
        minimalContact.setPhone("+1111111111");
        Long contactId = 456L;
        when(externalContactService.createExternalContactForCurrentUser(minimalContact)).thenReturn(contactId);

        ResponseEntity<Long> response = externalContactController.createExternalContactForCurrentUser(minimalContact);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(contactId, response.getBody());
        verify(externalContactService).createExternalContactForCurrentUser(minimalContact);
    }

    @Test
    void editExternalContact_shouldReturnNoContent() {
        Long contactId = 5L;
        ExternalContactDto updatedDto = new ExternalContactDto();
        updatedDto.setName("Updated Name");
        doNothing().when(externalContactService).editExternalContact(contactId, updatedDto);

        ResponseEntity<Void> response = externalContactController.editExternalContact(contactId, updatedDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(externalContactService, times(1)).editExternalContact(contactId, updatedDto);
    }

    @Test
    void editExternalContact_withDifferentId_success() {
        Long differentId = 777L;
        ExternalContactDto dto = new ExternalContactDto();
        dto.setPhone("+9999999999");
        doNothing().when(externalContactService).editExternalContact(differentId, dto);

        ResponseEntity<Void> response = externalContactController.editExternalContact(differentId, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).editExternalContact(differentId, dto);
    }

    @Test
    void editExternalContact_changeName_success() {
        ExternalContactDto dto = new ExternalContactDto();
        dto.setName("New Name");
        doNothing().when(externalContactService).editExternalContact(CONTACT_ID, dto);

        ResponseEntity<Void> response = externalContactController.editExternalContact(CONTACT_ID, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).editExternalContact(CONTACT_ID, dto);
    }

    @Test
    void editExternalContact_changePhone_success() {
        ExternalContactDto dto = new ExternalContactDto();
        dto.setPhone("+1234567890");
        doNothing().when(externalContactService).editExternalContact(CONTACT_ID, dto);

        ResponseEntity<Void> response = externalContactController.editExternalContact(CONTACT_ID, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).editExternalContact(CONTACT_ID, dto);
    }

    @Test
    void editExternalContact_changeRelation_success() {
        ExternalContactDto dto = new ExternalContactDto();
        dto.setRelation("Brother");
        doNothing().when(externalContactService).editExternalContact(CONTACT_ID, dto);

        ResponseEntity<Void> response = externalContactController.editExternalContact(CONTACT_ID, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).editExternalContact(CONTACT_ID, dto);
    }

    @Test
    void editExternalContact_changeAllFields_success() {
        ExternalContactDto dto = new ExternalContactDto();
        dto.setName("Completely New Name");
        dto.setPhone("+5555555555");
        dto.setRelation("Colleague");
        doNothing().when(externalContactService).editExternalContact(CONTACT_ID, dto);

        ResponseEntity<Void> response = externalContactController.editExternalContact(CONTACT_ID, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).editExternalContact(CONTACT_ID, dto);
    }

    @Test
    void editExternalContact_multipleEdits_success() {
        Long id1 = 10L, id2 = 20L;
        ExternalContactDto dto1 = new ExternalContactDto();
        ExternalContactDto dto2 = new ExternalContactDto();
        doNothing().when(externalContactService).editExternalContact(anyLong(), any(ExternalContactDto.class));

        ResponseEntity<Void> response1 = externalContactController.editExternalContact(id1, dto1);
        ResponseEntity<Void> response2 = externalContactController.editExternalContact(id2, dto2);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        verify(externalContactService).editExternalContact(id1, dto1);
        verify(externalContactService).editExternalContact(id2, dto2);
    }

    @Test
    void deleteExternalContact_shouldReturnNoContent() {
        Long contactId = 10L;
        doNothing().when(externalContactService).deleteExternalContact(contactId);

        ResponseEntity<Void> response = externalContactController.deleteExternalContact(contactId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(externalContactService, times(1)).deleteExternalContact(contactId);
    }

    @Test
    void deleteExternalContact_withDifferentId_success() {
        Long differentId = 888L;
        doNothing().when(externalContactService).deleteExternalContact(differentId);

        ResponseEntity<Void> response = externalContactController.deleteExternalContact(differentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).deleteExternalContact(differentId);
    }

    @Test
    void deleteExternalContact_multipleDeletes_success() {
        Long id1 = 100L, id2 = 200L, id3 = 300L;
        doNothing().when(externalContactService).deleteExternalContact(anyLong());

        ResponseEntity<Void> response1 = externalContactController.deleteExternalContact(id1);
        ResponseEntity<Void> response2 = externalContactController.deleteExternalContact(id2);
        ResponseEntity<Void> response3 = externalContactController.deleteExternalContact(id3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(externalContactService).deleteExternalContact(id1);
        verify(externalContactService).deleteExternalContact(id2);
        verify(externalContactService).deleteExternalContact(id3);
    }

    @Test
    void deleteExternalContact_withZeroId_success() {
        Long zeroId = 0L;
        doNothing().when(externalContactService).deleteExternalContact(zeroId);

        ResponseEntity<Void> response = externalContactController.deleteExternalContact(zeroId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).deleteExternalContact(zeroId);
    }

    @Test
    void deleteExternalContact_withMaxLongId_success() {
        Long maxId = Long.MAX_VALUE;
        doNothing().when(externalContactService).deleteExternalContact(maxId);

        ResponseEntity<Void> response = externalContactController.deleteExternalContact(maxId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(externalContactService).deleteExternalContact(maxId);
    }

    @Test
    void createEditDelete_workflow_success() {
        ExternalContactDto createDto = new ExternalContactDto();
        createDto.setName("Test Contact");
        Long createdId = 999L;

        ExternalContactDto editDto = new ExternalContactDto();
        editDto.setName("Updated Contact");

        when(externalContactService.createExternalContactForCurrentUser(createDto)).thenReturn(createdId);
        doNothing().when(externalContactService).editExternalContact(createdId, editDto);
        doNothing().when(externalContactService).deleteExternalContact(createdId);

        // When - Create
        ResponseEntity<Long> createResponse = externalContactController.createExternalContactForCurrentUser(createDto);

        // Then - Create
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertEquals(createdId, createResponse.getBody());

        // When - Edit
        ResponseEntity<Void> editResponse = externalContactController.editExternalContact(createdId, editDto);

        // Then - Edit
        assertEquals(HttpStatus.NO_CONTENT, editResponse.getStatusCode());

        // When - Delete
        ResponseEntity<Void> deleteResponse = externalContactController.deleteExternalContact(createdId);

        // Then - Delete
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify all operations were called
        verify(externalContactService).createExternalContactForCurrentUser(createDto);
        verify(externalContactService).editExternalContact(createdId, editDto);
        verify(externalContactService).deleteExternalContact(createdId);
    }
}
