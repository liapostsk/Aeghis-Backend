package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.ParticipationDto;
import com.tfg.aegis.service.ParticipationService;
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
class ParticipationControllerTest {

    @Mock
    private ParticipationService participationService;

    @InjectMocks
    private ParticipationController participationController;

    private ParticipationDto participationDto;
    private static final Long PARTICIPATION_ID = 123L;
    private static final Long USER_ID = 456L;
    private static final Long JOURNEY_ID = 789L;

    @BeforeEach
    void setUp() {
        participationDto = new ParticipationDto();
        participationDto.setId(PARTICIPATION_ID);
        participationDto.setUserId(USER_ID);
        participationDto.setJourneyId(JOURNEY_ID);
    }
    
    @Test
    void getParticipation_success() {
        when(participationService.getParticipation(PARTICIPATION_ID)).thenReturn(participationDto);

        ResponseEntity<ParticipationDto> response = participationController.getParticipation(PARTICIPATION_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(participationDto, response.getBody());
        assertEquals(PARTICIPATION_ID, response.getBody().getId());
        assertEquals(USER_ID, response.getBody().getUserId());
        assertEquals(JOURNEY_ID, response.getBody().getJourneyId());
        verify(participationService).getParticipation(PARTICIPATION_ID);
    }

    @Test
    void getParticipation_withDifferentId_success() {
        Long differentId = 999L;
        ParticipationDto differentParticipation = new ParticipationDto();
        differentParticipation.setId(differentId);
        differentParticipation.setUserId(100L);
        differentParticipation.setJourneyId(200L);
        when(participationService.getParticipation(differentId)).thenReturn(differentParticipation);

        ResponseEntity<ParticipationDto> response = participationController.getParticipation(differentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentId, response.getBody().getId());
        assertEquals(100L, response.getBody().getUserId());
        assertEquals(200L, response.getBody().getJourneyId());
        verify(participationService).getParticipation(differentId);
    }

    @Test
    void getParticipation_multipleCalls_success() {
        Long id1 = 1L, id2 = 2L, id3 = 3L;
        ParticipationDto part1 = new ParticipationDto();
        part1.setId(id1);
        ParticipationDto part2 = new ParticipationDto();
        part2.setId(id2);
        ParticipationDto part3 = new ParticipationDto();
        part3.setId(id3);

        when(participationService.getParticipation(id1)).thenReturn(part1);
        when(participationService.getParticipation(id2)).thenReturn(part2);
        when(participationService.getParticipation(id3)).thenReturn(part3);

        ResponseEntity<ParticipationDto> response1 = participationController.getParticipation(id1);
        ResponseEntity<ParticipationDto> response2 = participationController.getParticipation(id2);
        ResponseEntity<ParticipationDto> response3 = participationController.getParticipation(id3);

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertEquals(id1, response1.getBody().getId());
        assertEquals(id2, response2.getBody().getId());
        assertEquals(id3, response3.getBody().getId());
        verify(participationService).getParticipation(id1);
        verify(participationService).getParticipation(id2);
        verify(participationService).getParticipation(id3);
    }

    // ============ CREATE PARTICIPATION TESTS ============

    @Test
    void createParticipation_success() {
        ParticipationDto newParticipation = new ParticipationDto();
        newParticipation.setUserId(USER_ID);
        newParticipation.setJourneyId(JOURNEY_ID);
        when(participationService.createParticipation(newParticipation)).thenReturn(PARTICIPATION_ID);

        ResponseEntity<Long> response = participationController.createParticipation(newParticipation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(PARTICIPATION_ID, response.getBody());
        verify(participationService).createParticipation(newParticipation);
    }

    @Test
    void createParticipation_withDifferentData_success() {
        ParticipationDto newParticipation = new ParticipationDto();
        newParticipation.setUserId(777L);
        newParticipation.setJourneyId(888L);
        Long newId = 555L;
        when(participationService.createParticipation(newParticipation)).thenReturn(newId);

        ResponseEntity<Long> response = participationController.createParticipation(newParticipation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(participationService).createParticipation(newParticipation);
    }

    @Test
    void createParticipation_withMinimalData_success() {
        ParticipationDto minimalParticipation = new ParticipationDto();
        minimalParticipation.setUserId(1L);
        minimalParticipation.setJourneyId(1L);
        Long newId = 1L;
        when(participationService.createParticipation(minimalParticipation)).thenReturn(newId);

        ResponseEntity<Long> response = participationController.createParticipation(minimalParticipation);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(participationService).createParticipation(minimalParticipation);
    }

    @Test
    void createParticipation_multipleParticipations_success() {
        ParticipationDto part1 = new ParticipationDto();
        part1.setUserId(10L);
        part1.setJourneyId(20L);
        
        ParticipationDto part2 = new ParticipationDto();
        part2.setUserId(30L);
        part2.setJourneyId(40L);
        
        ParticipationDto part3 = new ParticipationDto();
        part3.setUserId(50L);
        part3.setJourneyId(60L);

        when(participationService.createParticipation(part1)).thenReturn(100L);
        when(participationService.createParticipation(part2)).thenReturn(200L);
        when(participationService.createParticipation(part3)).thenReturn(300L);

        ResponseEntity<Long> response1 = participationController.createParticipation(part1);
        ResponseEntity<Long> response2 = participationController.createParticipation(part2);
        ResponseEntity<Long> response3 = participationController.createParticipation(part3);

        assertEquals(HttpStatus.CREATED, response1.getStatusCode());
        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertEquals(HttpStatus.CREATED, response3.getStatusCode());
        assertEquals(100L, response1.getBody());
        assertEquals(200L, response2.getBody());
        assertEquals(300L, response3.getBody());
        verify(participationService, times(3)).createParticipation(any(ParticipationDto.class));
    }

    @Test
    void updateParticipation_success() {
        ParticipationDto updateDto = new ParticipationDto();
        updateDto.setId(PARTICIPATION_ID);
        updateDto.setUserId(USER_ID);
        updateDto.setJourneyId(JOURNEY_ID);
        doNothing().when(participationService).updateParticipation(updateDto);

        ResponseEntity<Void> response = participationController.updateParticipation(updateDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(participationService).updateParticipation(updateDto);
    }

    @Test
    void updateParticipation_withDifferentData_success() {
        ParticipationDto updateDto = new ParticipationDto();
        updateDto.setId(999L);
        updateDto.setUserId(111L);
        updateDto.setJourneyId(222L);
        doNothing().when(participationService).updateParticipation(updateDto);

        ResponseEntity<Void> response = participationController.updateParticipation(updateDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(participationService).updateParticipation(updateDto);
    }

    @Test
    void updateParticipation_multipleUpdates_success() {
        ParticipationDto update1 = new ParticipationDto();
        update1.setId(1L);
        
        ParticipationDto update2 = new ParticipationDto();
        update2.setId(2L);
        
        ParticipationDto update3 = new ParticipationDto();
        update3.setId(3L);

        doNothing().when(participationService).updateParticipation(any(ParticipationDto.class));

        ResponseEntity<Void> response1 = participationController.updateParticipation(update1);
        ResponseEntity<Void> response2 = participationController.updateParticipation(update2);
        ResponseEntity<Void> response3 = participationController.updateParticipation(update3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(participationService).updateParticipation(update1);
        verify(participationService).updateParticipation(update2);
        verify(participationService).updateParticipation(update3);
    }

    @Test
    void createGetUpdate_workflow_success() {
        ParticipationDto createDto = new ParticipationDto();
        createDto.setUserId(USER_ID);
        createDto.setJourneyId(JOURNEY_ID);

        ParticipationDto createdDto = new ParticipationDto();
        createdDto.setId(999L);
        createdDto.setUserId(USER_ID);
        createdDto.setJourneyId(JOURNEY_ID);

        ParticipationDto updateDto = new ParticipationDto();
        updateDto.setId(999L);
        updateDto.setUserId(USER_ID);
        updateDto.setJourneyId(888L); // Journey changed

        when(participationService.createParticipation(createDto)).thenReturn(999L);
        when(participationService.getParticipation(999L)).thenReturn(createdDto);
        doNothing().when(participationService).updateParticipation(updateDto);

        ResponseEntity<Long> createResponse = participationController.createParticipation(createDto);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals(999L, createResponse.getBody());

        ResponseEntity<ParticipationDto> getResponse = participationController.getParticipation(999L);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(999L, getResponse.getBody().getId());
        assertEquals(USER_ID, getResponse.getBody().getUserId());

        ResponseEntity<Void> updateResponse = participationController.updateParticipation(updateDto);

        assertEquals(HttpStatus.NO_CONTENT, updateResponse.getStatusCode());

        verify(participationService).createParticipation(createDto);
        verify(participationService).getParticipation(999L);
        verify(participationService).updateParticipation(updateDto);
    }

    @Test
    void multipleParticipationsWorkflow_success() {
        ParticipationDto part1 = new ParticipationDto();
        part1.setUserId(1L);
        part1.setJourneyId(10L);
        
        ParticipationDto part2 = new ParticipationDto();
        part2.setUserId(2L);
        part2.setJourneyId(20L);

        when(participationService.createParticipation(part1)).thenReturn(100L);
        when(participationService.createParticipation(part2)).thenReturn(200L);

        ResponseEntity<Long> create1 = participationController.createParticipation(part1);
        ResponseEntity<Long> create2 = participationController.createParticipation(part2);

        assertEquals(100L, create1.getBody());
        assertEquals(200L, create2.getBody());

        ParticipationDto retrieved1 = new ParticipationDto();
        retrieved1.setId(100L);
        ParticipationDto retrieved2 = new ParticipationDto();
        retrieved2.setId(200L);

        when(participationService.getParticipation(100L)).thenReturn(retrieved1);
        when(participationService.getParticipation(200L)).thenReturn(retrieved2);

        ResponseEntity<ParticipationDto> get1 = participationController.getParticipation(100L);
        ResponseEntity<ParticipationDto> get2 = participationController.getParticipation(200L);

        assertEquals(100L, get1.getBody().getId());
        assertEquals(200L, get2.getBody().getId());

        verify(participationService).createParticipation(part1);
        verify(participationService).createParticipation(part2);
        verify(participationService).getParticipation(100L);
        verify(participationService).getParticipation(200L);
    }
}
