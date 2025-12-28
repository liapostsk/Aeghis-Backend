package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.model.dto.InvitationDto;
import com.tfg.aegis.service.InvitationService;
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
class InvitationControllerTest {

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private InvitationController invitationController;

    private InvitationDto invitationDto;
    private GroupDto groupDto;
    private static final Long GROUP_ID = 123L;
    private static final String VALID_CODE = "INV-ABC123";

    @BeforeEach
    void setUp() {
        invitationDto = new InvitationDto();
        invitationDto.setCode(VALID_CODE);
        invitationDto.setGroupId(GROUP_ID);

        groupDto = new GroupDto();
        groupDto.setId(GROUP_ID);
        groupDto.setName("Test Group");
    }

    @Test
    void createInvitation_shouldReturn201WithBody_andPassNullExpiry() {
        Long expiryIgnoredByController = 999L; // el controller lo ignora y pasa null
        when(invitationService.createInvitation(GROUP_ID, null)).thenReturn(invitationDto);

        ResponseEntity<InvitationDto> response =
                invitationController.createInvitation(GROUP_ID, expiryIgnoredByController);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(invitationDto, response.getBody());
        assertEquals(VALID_CODE, response.getBody().getCode());
        verify(invitationService, times(1)).createInvitation(GROUP_ID, null);
    }

    @Test
    void createInvitation_withoutExpiryParam_shouldPassNull() {
        when(invitationService.createInvitation(GROUP_ID, null)).thenReturn(invitationDto);

        ResponseEntity<InvitationDto> response =
                invitationController.createInvitation(GROUP_ID, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(invitationDto, response.getBody());
        verify(invitationService, times(1)).createInvitation(GROUP_ID, null);
    }

    @Test
    void createInvitation_withDifferentGroupId_success() {
        Long differentGroupId = 456L;
        InvitationDto differentInvitation = new InvitationDto();
        differentInvitation.setGroupId(differentGroupId);
        differentInvitation.setCode("INV-XYZ789");
        when(invitationService.createInvitation(differentGroupId, null)).thenReturn(differentInvitation);

        ResponseEntity<InvitationDto> response =
                invitationController.createInvitation(differentGroupId, 100L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentGroupId, response.getBody().getGroupId());
        assertEquals("INV-XYZ789", response.getBody().getCode());
        verify(invitationService).createInvitation(differentGroupId, null);
    }

    @Test
    void createInvitation_withZeroExpiry_stillPassesNull() {
        Long zeroExpiry = 0L;
        when(invitationService.createInvitation(GROUP_ID, null)).thenReturn(invitationDto);

        ResponseEntity<InvitationDto> response =
                invitationController.createInvitation(GROUP_ID, zeroExpiry);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(invitationService).createInvitation(GROUP_ID, null);
    }

    @Test
    void createInvitation_withNegativeExpiry_stillPassesNull() {
        Long negativeExpiry = -100L;
        when(invitationService.createInvitation(GROUP_ID, null)).thenReturn(invitationDto);

        ResponseEntity<InvitationDto> response =
                invitationController.createInvitation(GROUP_ID, negativeExpiry);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(invitationService).createInvitation(GROUP_ID, null);
    }

    @Test
    void createInvitation_multipleCallsSameGroup_eachReturns201() {
        InvitationDto invitation1 = new InvitationDto();
        invitation1.setCode("CODE-1");
        invitation1.setGroupId(GROUP_ID);

        InvitationDto invitation2 = new InvitationDto();
        invitation2.setCode("CODE-2");
        invitation2.setGroupId(GROUP_ID);

        when(invitationService.createInvitation(GROUP_ID, null))
                .thenReturn(invitation1)
                .thenReturn(invitation2);

        ResponseEntity<InvitationDto> response1 = invitationController.createInvitation(GROUP_ID, null);
        ResponseEntity<InvitationDto> response2 = invitationController.createInvitation(GROUP_ID, null);

        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertEquals("CODE-1", response1.getBody().getCode());
        assertEquals("CODE-2", response2.getBody().getCode());
        verify(invitationService, times(2)).createInvitation(GROUP_ID, null);
    }

    @Test
    void validateInvitation_shouldReturn200WithGroupDto() {
        when(invitationService.validateInvitation(VALID_CODE)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(VALID_CODE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(groupDto, response.getBody());
        assertEquals(GROUP_ID, response.getBody().getId());
        assertEquals("Test Group", response.getBody().getName());
        verify(invitationService, times(1)).validateInvitation(VALID_CODE);
    }

    @Test
    void validateInvitation_withDifferentCode_success() {
        String differentCode = "DIFFERENT-CODE-456";
        GroupDto differentGroup = new GroupDto();
        differentGroup.setId(999L);
        differentGroup.setName("Different Group");
        when(invitationService.validateInvitation(differentCode)).thenReturn(differentGroup);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(differentCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(999L, response.getBody().getId());
        assertEquals("Different Group", response.getBody().getName());
        verify(invitationService).validateInvitation(differentCode);
    }

    @Test
    void validateInvitation_withShortCode_success() {
        String shortCode = "ABC";
        when(invitationService.validateInvitation(shortCode)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(shortCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(invitationService).validateInvitation(shortCode);
    }

    @Test
    void validateInvitation_withLongCode_success() {
        String longCode = "VERY-LONG-INVITATION-CODE-WITH-MANY-CHARACTERS-123456789";
        when(invitationService.validateInvitation(longCode)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(longCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(invitationService).validateInvitation(longCode);
    }

    @Test
    void validateInvitation_withNumericCode_success() {
        String numericCode = "123456";
        when(invitationService.validateInvitation(numericCode)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(numericCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invitationService).validateInvitation(numericCode);
    }

    @Test
    void validateInvitation_withSpecialCharacters_success() {
        String specialCode = "CODE-WITH_SPECIAL.CHARS!";
        when(invitationService.validateInvitation(specialCode)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = invitationController.validateInvitation(specialCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invitationService).validateInvitation(specialCode);
    }

    @Test
    void validateInvitation_multipleCalls_eachCallsService() {
        String code1 = "CODE1";
        String code2 = "CODE2";

        GroupDto group1 = new GroupDto();
        group1.setId(1L);

        GroupDto group2 = new GroupDto();
        group2.setId(2L);

        when(invitationService.validateInvitation(code1)).thenReturn(group1);
        when(invitationService.validateInvitation(code2)).thenReturn(group2);

        ResponseEntity<GroupDto> response1 = invitationController.validateInvitation(code1);
        ResponseEntity<GroupDto> response2 = invitationController.validateInvitation(code2);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(1L, response1.getBody().getId());
        assertEquals(2L, response2.getBody().getId());
        verify(invitationService).validateInvitation(code1);
        verify(invitationService).validateInvitation(code2);
    }

    @Test
    void createInvitation_alwaysIgnoresExpiryParameter() {
        Long[] expiryValues = {null, 0L, 1L, 100L, 9999L, -1L, Long.MAX_VALUE, Long.MIN_VALUE};

        for (Long expiry : expiryValues) {
            reset(invitationService);
            when(invitationService.createInvitation(GROUP_ID, null)).thenReturn(invitationDto);

                ResponseEntity<InvitationDto> response = invitationController.createInvitation(GROUP_ID, expiry);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(invitationService).createInvitation(eq(GROUP_ID), isNull());
        }
    }
}
