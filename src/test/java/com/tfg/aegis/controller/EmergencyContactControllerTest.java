package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.EmergencyContactDto;
import com.tfg.aegis.model.dto.EmergencyTriggerRequestDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.enums.EmergencyContactEnum;
import com.tfg.aegis.service.EmergencyAlertService;
import com.tfg.aegis.service.EmergencyContactService;
import com.tfg.aegis.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactControllerTest {

    @Mock
    private EmergencyContactService emergencyContactService;

    @Mock
    private UserService userService;

    @Mock
    private EmergencyAlertService emergencyAlertService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EmergencyContactController emergencyContactController;

    private EmergencyContactDto emergencyContactDto;
    private UserDto userDto;
    private static final Long CONTACT_ID = 123L;
    private static final Long USER_ID = 1L;
    private static final String CLERK_ID = "clerk_user123";
    private static final String USER_NAME = "Test User";

    @BeforeEach
    void setUp() {
        emergencyContactDto = new EmergencyContactDto();
        emergencyContactDto.setId(CONTACT_ID);
        emergencyContactDto.setContactId(2L);
        emergencyContactDto.setRelation("Sister");
        emergencyContactDto.setStatus(EmergencyContactEnum.Status.PENDING);

        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setClerkId(CLERK_ID);
        userDto.setName(USER_NAME);

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(CLERK_ID);
        lenient().when(userService.getUserByClerkId(CLERK_ID)).thenReturn(userDto);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addEmergencyContactForCurrentUser_shouldReturnOkWithDto() {
        EmergencyContactDto inputDto = new EmergencyContactDto();
        inputDto.setContactId(5L);
        inputDto.setRelation("Brother");
        EmergencyContactDto expectedDto = new EmergencyContactDto();
        expectedDto.setId(999L);

        when(emergencyContactService.addEmergencyContactForCurrentUser(inputDto)).thenReturn(expectedDto);

        ResponseEntity<EmergencyContactDto> response =
                emergencyContactController.addEmergencyContactForCurrentUser(inputDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDto, response.getBody());
        assertEquals(999L, response.getBody().getId());
        verify(emergencyContactService, times(1)).addEmergencyContactForCurrentUser(inputDto);
    }

    @Test
    void addEmergencyContactForCurrentUser_withDifferentRelation_success() {
        EmergencyContactDto inputDto = new EmergencyContactDto();
        inputDto.setRelation("Friend");
        EmergencyContactDto returnedDto = new EmergencyContactDto();
        returnedDto.setRelation("Friend");

        when(emergencyContactService.addEmergencyContactForCurrentUser(inputDto)).thenReturn(returnedDto);

        ResponseEntity<EmergencyContactDto> response =
                emergencyContactController.addEmergencyContactForCurrentUser(inputDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Friend", response.getBody().getRelation());
        verify(emergencyContactService).addEmergencyContactForCurrentUser(inputDto);
    }

    @Test
    void addEmergencyContactForCurrentUser_multipleContacts_success() {
        EmergencyContactDto contact1 = new EmergencyContactDto();
        contact1.setContactId(10L);
        EmergencyContactDto contact2 = new EmergencyContactDto();
        contact2.setContactId(20L);

        EmergencyContactDto result1 = new EmergencyContactDto();
        result1.setId(100L);
        EmergencyContactDto result2 = new EmergencyContactDto();
        result2.setId(200L);

        when(emergencyContactService.addEmergencyContactForCurrentUser(contact1)).thenReturn(result1);
        when(emergencyContactService.addEmergencyContactForCurrentUser(contact2)).thenReturn(result2);

        ResponseEntity<EmergencyContactDto> response1 =
                emergencyContactController.addEmergencyContactForCurrentUser(contact1);
        ResponseEntity<EmergencyContactDto> response2 =
                emergencyContactController.addEmergencyContactForCurrentUser(contact2);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(100L, response1.getBody().getId());
        assertEquals(200L, response2.getBody().getId());
        verify(emergencyContactService, times(2)).addEmergencyContactForCurrentUser(any(EmergencyContactDto.class));
    }

    @Test
    void editEmergencyContact_shouldReturnNoContent() {
        Long contactId = 1L;
        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setRelation("Updated Relation");
        doNothing().when(emergencyContactService).editEmergencyContact(contactId, dto);

        ResponseEntity<Void> response =
                emergencyContactController.editEmergencyContact(contactId, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(emergencyContactService, times(1)).editEmergencyContact(contactId, dto);
    }

    @Test
    void editEmergencyContact_withDifferentId_success() {
        Long contactId = 555L;
        EmergencyContactDto dto = new EmergencyContactDto();
        doNothing().when(emergencyContactService).editEmergencyContact(contactId, dto);

        ResponseEntity<Void> response =
                emergencyContactController.editEmergencyContact(contactId, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyContactService).editEmergencyContact(contactId, dto);
    }

    @Test
    void editEmergencyContact_changeRelation_success() {
        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setRelation("Mother");
        doNothing().when(emergencyContactService).editEmergencyContact(CONTACT_ID, dto);

        ResponseEntity<Void> response =
                emergencyContactController.editEmergencyContact(CONTACT_ID, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyContactService).editEmergencyContact(CONTACT_ID, dto);
    }

    @Test
    void editEmergencyContact_changeStatus_success() {
        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setStatus(EmergencyContactEnum.Status.ACCEPTED);
        doNothing().when(emergencyContactService).editEmergencyContact(CONTACT_ID, dto);

        ResponseEntity<Void> response =
                emergencyContactController.editEmergencyContact(CONTACT_ID, dto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyContactService).editEmergencyContact(CONTACT_ID, dto);
    }

    @Test
    void deleteEmergencyContact_shouldReturnNoContent() {
        Long contactId = 2L;
        doNothing().when(emergencyContactService).deleteEmergencyContactForCurrentUser(contactId);

        ResponseEntity<Void> response =
                emergencyContactController.deleteEmergencyContact(contactId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(emergencyContactService, times(1)).deleteEmergencyContactForCurrentUser(contactId);
    }

    @Test
    void deleteEmergencyContact_withDifferentId_success() {
        Long contactId = 888L;
        doNothing().when(emergencyContactService).deleteEmergencyContactForCurrentUser(contactId);

        ResponseEntity<Void> response =
                emergencyContactController.deleteEmergencyContact(contactId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyContactService).deleteEmergencyContactForCurrentUser(contactId);
    }

    @Test
    void deleteEmergencyContact_multipleDeletes_success() {
        Long id1 = 10L, id2 = 20L, id3 = 30L;
        doNothing().when(emergencyContactService).deleteEmergencyContactForCurrentUser(anyLong());

        ResponseEntity<Void> response1 = emergencyContactController.deleteEmergencyContact(id1);
        ResponseEntity<Void> response2 = emergencyContactController.deleteEmergencyContact(id2);
        ResponseEntity<Void> response3 = emergencyContactController.deleteEmergencyContact(id3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(emergencyContactService).deleteEmergencyContactForCurrentUser(id1);
        verify(emergencyContactService).deleteEmergencyContactForCurrentUser(id2);
        verify(emergencyContactService).deleteEmergencyContactForCurrentUser(id3);
    }

    @Test
    void triggerEmergency_success() {
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3851);
        request.setLongitude(2.1734);
        request.setMessage("Help needed!");

        doNothing().when(emergencyAlertService).trigger(USER_ID, USER_NAME, request);

        ResponseEntity<Void> response = emergencyContactController.triggerEmergency(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(emergencyAlertService).trigger(USER_ID, USER_NAME, request);
    }

    @Test
    void triggerEmergency_withDifferentMessage_success() {
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(40.4168);
        request.setLongitude(-3.7038);
        request.setMessage("Emergency situation!");

        doNothing().when(emergencyAlertService).trigger(USER_ID, USER_NAME, request);

        ResponseEntity<Void> response = emergencyContactController.triggerEmergency(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyAlertService).trigger(USER_ID, USER_NAME, request);
    }

    @Test
    void triggerEmergency_withNoMessage_success() {
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(39.4699);
        request.setLongitude(-0.3763);

        doNothing().when(emergencyAlertService).trigger(USER_ID, USER_NAME, request);

        ResponseEntity<Void> response = emergencyContactController.triggerEmergency(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyAlertService).trigger(USER_ID, USER_NAME, request);
    }

    @Test
    void triggerEmergency_withDifferentCoordinates_success() {
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(51.5074);
        request.setLongitude(-0.1278);
        request.setMessage("Urgent!");

        doNothing().when(emergencyAlertService).trigger(USER_ID, USER_NAME, request);

        ResponseEntity<Void> response = emergencyContactController.triggerEmergency(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(emergencyAlertService).trigger(eq(USER_ID), eq(USER_NAME), eq(request));
    }

    @Test
    void triggerEmergency_verifySecurityContextUsed_success() {
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setMessage("Test emergency");

        doNothing().when(emergencyAlertService).trigger(anyLong(), anyString(), any(EmergencyTriggerRequestDto.class));

        emergencyContactController.triggerEmergency(request);

        verify(securityContext).getAuthentication();
        verify(authentication).getPrincipal();
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(emergencyAlertService).trigger(USER_ID, USER_NAME, request);
    }

    @Test
    void triggerEmergency_multipleTriggers_success() {
        EmergencyTriggerRequestDto request1 = new EmergencyTriggerRequestDto();
        request1.setMessage("First emergency");
        EmergencyTriggerRequestDto request2 = new EmergencyTriggerRequestDto();
        request2.setMessage("Second emergency");

        doNothing().when(emergencyAlertService).trigger(anyLong(), anyString(), any(EmergencyTriggerRequestDto.class));

        ResponseEntity<Void> response1 = emergencyContactController.triggerEmergency(request1);
        ResponseEntity<Void> response2 = emergencyContactController.triggerEmergency(request2);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        verify(emergencyAlertService).trigger(USER_ID, USER_NAME, request1);
        verify(emergencyAlertService).trigger(USER_ID, USER_NAME, request2);
    }

    @Test
    void addEditDelete_workflow_success() {
        EmergencyContactDto addDto = new EmergencyContactDto();
        addDto.setContactId(5L);
        addDto.setRelation("Friend");

        EmergencyContactDto addedDto = new EmergencyContactDto();
        addedDto.setId(100L);
        addedDto.setContactId(5L);
        addedDto.setRelation("Friend");

        EmergencyContactDto editDto = new EmergencyContactDto();
        editDto.setRelation("Best Friend");

        when(emergencyContactService.addEmergencyContactForCurrentUser(addDto)).thenReturn(addedDto);
        doNothing().when(emergencyContactService).editEmergencyContact(100L, editDto);
        doNothing().when(emergencyContactService).deleteEmergencyContactForCurrentUser(100L);

        // When - Add
        ResponseEntity<EmergencyContactDto> addResponse =
                emergencyContactController.addEmergencyContactForCurrentUser(addDto);

        // Then - Add
        assertEquals(HttpStatus.OK, addResponse.getStatusCode());
        assertEquals(100L, addResponse.getBody().getId());

        // When - Edit
        ResponseEntity<Void> editResponse =
                emergencyContactController.editEmergencyContact(100L, editDto);

        // Then - Edit
        assertEquals(HttpStatus.NO_CONTENT, editResponse.getStatusCode());

        // When - Delete
        ResponseEntity<Void> deleteResponse =
                emergencyContactController.deleteEmergencyContact(100L);

        // Then - Delete
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verify all operations
        verify(emergencyContactService).addEmergencyContactForCurrentUser(addDto);
        verify(emergencyContactService).editEmergencyContact(100L, editDto);
        verify(emergencyContactService).deleteEmergencyContactForCurrentUser(100L);
    }
}
