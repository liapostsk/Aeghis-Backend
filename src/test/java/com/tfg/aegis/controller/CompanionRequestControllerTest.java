package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.CompanionRequestDto;
import com.tfg.aegis.model.dto.CreateCompanionRequestDto;
import com.tfg.aegis.model.dto.JourneyDto;
import com.tfg.aegis.model.enums.CompanionRequestEnums;
import com.tfg.aegis.service.CompanionRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanionRequestControllerTest {

    @Mock
    private CompanionRequestService companionRequestService;

    @InjectMocks
    private CompanionRequestController companionRequestController;

    private CompanionRequestDto companionRequestDto;
    private CreateCompanionRequestDto createCompanionRequestDto;
    private JourneyDto journeyDto;
    private static final Long REQUEST_ID = 123L;
    private static final Long GROUP_ID = 456L;

    @BeforeEach
    void setUp() {
        companionRequestDto = new CompanionRequestDto();
        companionRequestDto.setId(REQUEST_ID);
        companionRequestDto.setDescription("Test companion request");

        createCompanionRequestDto = new CreateCompanionRequestDto();
        createCompanionRequestDto.setDescription("Create test");
        createCompanionRequestDto.setSourceId(1L);
        createCompanionRequestDto.setDestinationId(2L);

        journeyDto = new JourneyDto();
        journeyDto.setId(1L);
        journeyDto.setGroupId(GROUP_ID);
    }

    @Test
    void createCompanionRequest_success() {
        when(companionRequestService.createCompanionRequest(createCompanionRequestDto)).thenReturn(REQUEST_ID);

        ResponseEntity<Long> response = companionRequestController.createCompanionRequest(createCompanionRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(REQUEST_ID, response.getBody());
        verify(companionRequestService).createCompanionRequest(createCompanionRequestDto);
    }

    @Test
    void createCompanionRequest_withDifferentData_success() {
        CreateCompanionRequestDto dto = new CreateCompanionRequestDto();
        dto.setDescription("Different request");
        Long newId = 999L;
        when(companionRequestService.createCompanionRequest(dto)).thenReturn(newId);

        ResponseEntity<Long> response = companionRequestController.createCompanionRequest(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(companionRequestService).createCompanionRequest(dto);
    }

    @Test
    void deleteCompanionRequest_success() {
        doNothing().when(companionRequestService).deleteCompanionRequest(REQUEST_ID);

        ResponseEntity<Void> response = companionRequestController.deleteCompanionRequest(REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(companionRequestService).deleteCompanionRequest(REQUEST_ID);
    }

    @Test
    void deleteCompanionRequest_withDifferentId_success() {
        Long differentId = 555L;
        doNothing().when(companionRequestService).deleteCompanionRequest(differentId);

        ResponseEntity<Void> response = companionRequestController.deleteCompanionRequest(differentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(companionRequestService).deleteCompanionRequest(differentId);
    }

    @Test
    void editCompanionRequest_success() {
        when(companionRequestService.editCompanionRequest(REQUEST_ID, createCompanionRequestDto))
                .thenReturn(companionRequestDto);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.editCompanionRequest(REQUEST_ID, createCompanionRequestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(companionRequestDto, response.getBody());
        verify(companionRequestService).editCompanionRequest(REQUEST_ID, createCompanionRequestDto);
    }

    @Test
    void editCompanionRequest_withDifferentData_success() {
        Long requestId = 444L;
        CreateCompanionRequestDto dto = new CreateCompanionRequestDto();
        dto.setDescription("Updated description");
        CompanionRequestDto result = new CompanionRequestDto();
        when(companionRequestService.editCompanionRequest(requestId, dto)).thenReturn(result);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.editCompanionRequest(requestId, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companionRequestService).editCompanionRequest(requestId, dto);
    }

    @Test
    void linkGroupToCompanionRequest_success() {
        when(companionRequestService.linkGroupToCompanionRequest(REQUEST_ID, GROUP_ID))
                .thenReturn(companionRequestDto);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.linkGroupToCompanionRequest(REQUEST_ID, GROUP_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(companionRequestDto, response.getBody());
        verify(companionRequestService).linkGroupToCompanionRequest(REQUEST_ID, GROUP_ID);
    }

    @Test
    void linkGroupToCompanionRequest_withDifferentIds_success() {
        Long requestId = 100L;
        Long groupId = 200L;
        CompanionRequestDto result = new CompanionRequestDto();
        when(companionRequestService.linkGroupToCompanionRequest(requestId, groupId)).thenReturn(result);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.linkGroupToCompanionRequest(requestId, groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companionRequestService).linkGroupToCompanionRequest(requestId, groupId);
    }

    @Test
    void getCompanionRequestByCompanionGroupId_success() {
        when(companionRequestService.getCompanionRequestByCompanionGroupId(GROUP_ID))
                .thenReturn(companionRequestDto);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.getCompanionRequestByCompanionGroupId(GROUP_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(companionRequestDto, response.getBody());
        verify(companionRequestService).getCompanionRequestByCompanionGroupId(GROUP_ID);
    }

    @Test
    void getCompanionRequestByCompanionGroupId_withDifferentId_success() {
        Long groupId = 999L;
        CompanionRequestDto result = new CompanionRequestDto();
        when(companionRequestService.getCompanionRequestByCompanionGroupId(groupId)).thenReturn(result);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.getCompanionRequestByCompanionGroupId(groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companionRequestService).getCompanionRequestByCompanionGroupId(groupId);
    }

    @Test
    void getMyCompanionRequests_withRequests_success() {
        CompanionRequestDto req1 = new CompanionRequestDto();
        req1.setId(1L);
        CompanionRequestDto req2 = new CompanionRequestDto();
        req2.setId(2L);
        List<CompanionRequestDto> requests = Arrays.asList(req1, req2);
        when(companionRequestService.getMyCompanionRequests()).thenReturn(requests);

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.getMyCompanionRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(companionRequestService).getMyCompanionRequests();
    }

    @Test
    void getMyCompanionRequests_noRequests_success() {
        when(companionRequestService.getMyCompanionRequests()).thenReturn(Collections.emptyList());

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.getMyCompanionRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(companionRequestService).getMyCompanionRequests();
    }

    @Test
    void listActiveCompanionRequests_withRequests_success() {
        List<CompanionRequestDto> requests = Arrays.asList(companionRequestDto);
        when(companionRequestService.listActiveCompanionRequests()).thenReturn(requests);

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.listActiveCompanionRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(companionRequestService).listActiveCompanionRequests();
    }

    @Test
    void listActiveCompanionRequests_noRequests_success() {
        when(companionRequestService.listActiveCompanionRequests()).thenReturn(Collections.emptyList());

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.listActiveCompanionRequests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(companionRequestService).listActiveCompanionRequests();
    }

    @Test
    void searchCompanionRequests_withAllParams_success() {
        Long destinoId = 10L;
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusHours(3);
        boolean excludeMine = true;
        List<CompanionRequestDto> requests = Arrays.asList(companionRequestDto);
        when(companionRequestService.searchCompanionRequests(destinoId, from, to, excludeMine))
                .thenReturn(requests);

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.searchCompanionRequests(destinoId, from, to, excludeMine);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(companionRequestService).searchCompanionRequests(destinoId, from, to, excludeMine);
    }

    @Test
    void searchCompanionRequests_withNullParams_success() {
        List<CompanionRequestDto> requests = Collections.emptyList();
        when(companionRequestService.searchCompanionRequests(null, null, null, true))
                .thenReturn(requests);

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.searchCompanionRequests(null, null, null, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companionRequestService).searchCompanionRequests(null, null, null, true);
    }

    @Test
    void searchCompanionRequests_excludeMine_false_success() {
        List<CompanionRequestDto> requests = Arrays.asList(companionRequestDto);
        when(companionRequestService.searchCompanionRequests(anyLong(), any(), any(), eq(false)))
                .thenReturn(requests);

        ResponseEntity<List<CompanionRequestDto>> response = 
                companionRequestController.searchCompanionRequests(5L, LocalDateTime.now(), 
                        LocalDateTime.now().plusDays(1), false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(companionRequestService).searchCompanionRequests(anyLong(), any(), any(), eq(false));
    }

    @Test
    void requestToJoinCompanionRequest_withMessage_success() {
        String message = "I would like to join";
        doNothing().when(companionRequestService).requestToJoinCompanionRequest(REQUEST_ID, message);

        ResponseEntity<Void> response = 
                companionRequestController.requestToJoinCompanionRequest(REQUEST_ID, message);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(companionRequestService).requestToJoinCompanionRequest(REQUEST_ID, message);
    }

    @Test
    void requestToJoinCompanionRequest_withoutMessage_success() {
        doNothing().when(companionRequestService).requestToJoinCompanionRequest(REQUEST_ID, null);

        ResponseEntity<Void> response = 
                companionRequestController.requestToJoinCompanionRequest(REQUEST_ID, null);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(companionRequestService).requestToJoinCompanionRequest(REQUEST_ID, null);
    }

    @Test
    void requestToJoinCompanionRequest_withDifferentId_success() {
        Long requestId = 777L;
        String message = "Test message";
        doNothing().when(companionRequestService).requestToJoinCompanionRequest(requestId, message);

        ResponseEntity<Void> response = 
                companionRequestController.requestToJoinCompanionRequest(requestId, message);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(companionRequestService).requestToJoinCompanionRequest(requestId, message);
    }

    @Test
    void cancelCompanionRequest_success() {
        doNothing().when(companionRequestService).cancelCompanionRequest(REQUEST_ID);

        ResponseEntity<Void> response = companionRequestController.cancelCompanionRequest(REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(companionRequestService).cancelCompanionRequest(REQUEST_ID);
    }

    @Test
    void cancelCompanionRequest_withDifferentId_success() {
        Long requestId = 888L;
        doNothing().when(companionRequestService).cancelCompanionRequest(requestId);

        ResponseEntity<Void> response = companionRequestController.cancelCompanionRequest(requestId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(companionRequestService).cancelCompanionRequest(requestId);
    }

    @Test
    void getCompanionRequestById_success() {
        when(companionRequestService.getCompanionRequestById(REQUEST_ID)).thenReturn(companionRequestDto);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.getCompanionRequestById(REQUEST_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(companionRequestDto, response.getBody());
        verify(companionRequestService).getCompanionRequestById(REQUEST_ID);
    }

    @Test
    void getCompanionRequestById_withDifferentId_success() {
        Long requestId = 666L;
        CompanionRequestDto dto = new CompanionRequestDto();
        dto.setId(requestId);
        when(companionRequestService.getCompanionRequestById(requestId)).thenReturn(dto);

        ResponseEntity<CompanionRequestDto> response = 
                companionRequestController.getCompanionRequestById(requestId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(requestId, response.getBody().getId());
        verify(companionRequestService).getCompanionRequestById(requestId);
    }

    @Test
    void changeStatus_toMatched_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        updatedDto.setId(REQUEST_ID);
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.MATCHED))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.MATCHED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(REQUEST_ID, response.getBody().getId());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.MATCHED);
    }

    @Test
    void changeStatus_toCreated_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.CREATED))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.CREATED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.CREATED);
    }

    @Test
    void changeStatus_toFinished_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.FINISHED))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.FINISHED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.FINISHED);
    }

    @Test
    void changeStatus_toPending_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.PENDING))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.PENDING);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.PENDING);
    }

    @Test
    void changeStatus_toInProgress_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.IN_PROGRESS))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.IN_PROGRESS);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.IN_PROGRESS);
    }

    @Test
    void changeStatus_toCancelled_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.CANCELLED))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.CANCELLED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.CANCELLED);
    }

    @Test
    void changeStatus_toExpired_success() {
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        when(companionRequestService.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.EXPIRED))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.EXPIRED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(companionRequestService).changeStatus(REQUEST_ID, CompanionRequestEnums.RequestStatus.EXPIRED);
    }

    @Test
    void changeStatus_withDifferentId_success() {
        Long differentId = 999L;
        CompanionRequestDto updatedDto = new CompanionRequestDto();
        updatedDto.setId(differentId);
        when(companionRequestService.changeStatus(differentId, CompanionRequestEnums.RequestStatus.MATCHED))
                .thenReturn(updatedDto);

        ResponseEntity<CompanionRequestDto> response =
                companionRequestController.changeStatus(differentId, CompanionRequestEnums.RequestStatus.MATCHED);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(differentId, response.getBody().getId());
        verify(companionRequestService).changeStatus(differentId, CompanionRequestEnums.RequestStatus.MATCHED);
    }
}
