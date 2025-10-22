package com.tfg.aegis.invitation;

import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.invitation.model.InvitationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvitationControllerTest {

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private InvitationController invitationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInvitation_shouldReturn201WithBody_andPassNullExpiry() {
        Long groupId = 123L;
        Long expiryIgnoredByController = 999L; // el controller lo ignora y pasa null
        InvitationDto expected = new InvitationDto();

        when(invitationService.createInvitation(groupId, null)).thenReturn(expected);

        ResponseEntity<InvitationDto> response =
                invitationController.createInvitation(groupId, expiryIgnoredByController);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(invitationService, times(1)).createInvitation(groupId, null);
    }

    @Test
    void validateInvitation_shouldReturn200WithGroupDto() {
        String code = "INV-ABC";
        GroupDto allowed = new GroupDto();
        when(invitationService.validateInvitation(code)).thenReturn(allowed);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(allowed, response.getBody());
        verify(invitationService, times(1)).validateInvitation(code);
    }
}
