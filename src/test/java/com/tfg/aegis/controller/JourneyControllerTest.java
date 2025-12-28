package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.JourneyDto;
import com.tfg.aegis.service.JourneyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JourneyControllerTest {

    @Mock
    private JourneyService journeyService;

    @InjectMocks
    private JourneyController journeyController;

    private JourneyDto journeyDto;
    private static final Long JOURNEY_ID = 1L;
    private static final Long GROUP_ID = 1L;
    private static final Long PARTICIPATION_ID = 1L;

    @BeforeEach
    void setUp() {
        journeyDto = new JourneyDto();
        journeyDto.setId(JOURNEY_ID);
        journeyDto.setGroupId(GROUP_ID);
    }

    @Test
    void getJourney_success() {
        when(journeyService.getJourney(JOURNEY_ID)).thenReturn(journeyDto);

        ResponseEntity<JourneyDto> response = journeyController.getJourney(JOURNEY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(journeyDto, response.getBody());
        assertEquals(JOURNEY_ID, response.getBody().getId());
        verify(journeyService).getJourney(JOURNEY_ID);
    }

    @Test
    void getJourney_withDifferentId_success() {
        Long differentId = 5L;
        JourneyDto differentJourney = new JourneyDto();
        differentJourney.setId(differentId);
        when(journeyService.getJourney(differentId)).thenReturn(differentJourney);

        ResponseEntity<JourneyDto> response = journeyController.getJourney(differentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentId, response.getBody().getId());
        verify(journeyService).getJourney(differentId);
    }

    @Test
    void getCurrentJourneyForGroup_success() {
        when(journeyService.getCurrentJourneyForGroup(GROUP_ID)).thenReturn(journeyDto);

        ResponseEntity<JourneyDto> response = journeyController.getCurrentJourneyForGroup(GROUP_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(journeyDto, response.getBody());
        assertEquals(GROUP_ID, response.getBody().getGroupId());
        verify(journeyService).getCurrentJourneyForGroup(GROUP_ID);
    }

    @Test
    void getCurrentJourneyForGroup_withDifferentGroupId_success() {
        Long differentGroupId = 10L;
        JourneyDto groupJourney = new JourneyDto();
        groupJourney.setGroupId(differentGroupId);
        when(journeyService.getCurrentJourneyForGroup(differentGroupId)).thenReturn(groupJourney);

        ResponseEntity<JourneyDto> response = journeyController.getCurrentJourneyForGroup(differentGroupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentGroupId, response.getBody().getGroupId());
        verify(journeyService).getCurrentJourneyForGroup(differentGroupId);
    }

    @Test
    void getActiveJourneys_withJourneys_returnsSet() {
        JourneyDto journey1 = new JourneyDto();
        journey1.setId(1L);

        JourneyDto journey2 = new JourneyDto();
        journey2.setId(2L);

        Set<JourneyDto> activeJourneys = new HashSet<>();
        activeJourneys.add(journey1);
        activeJourneys.add(journey2);

        when(journeyService.getActiveJourneys()).thenReturn(activeJourneys);

        ResponseEntity<Set<JourneyDto>> response = journeyController.getActiveJourneys();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(journeyService).getActiveJourneys();
    }

    @Test
    void getActiveJourneys_noJourneys_returnsEmptySet() {
        when(journeyService.getActiveJourneys()).thenReturn(Collections.emptySet());

        ResponseEntity<Set<JourneyDto>> response = journeyController.getActiveJourneys();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(journeyService).getActiveJourneys();
    }

    @Test
    void createJourney_success() {
        JourneyDto newJourney = new JourneyDto();
        newJourney.setGroupId(GROUP_ID);
        when(journeyService.createJourney(any(JourneyDto.class))).thenReturn(JOURNEY_ID);

        ResponseEntity<Long> response = journeyController.createJourney(newJourney);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(JOURNEY_ID, response.getBody());
        verify(journeyService).createJourney(any(JourneyDto.class));
    }

    @Test
    void createJourney_withDifferentData_success() {
        JourneyDto newJourney = new JourneyDto();
        newJourney.setGroupId(5L);
        Long newId = 10L;
        when(journeyService.createJourney(any(JourneyDto.class))).thenReturn(newId);

        ResponseEntity<Long> response = journeyController.createJourney(newJourney);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(journeyService).createJourney(newJourney);
    }

    @Test
    void updateJourney_success() {
        JourneyDto updatedJourney = new JourneyDto();
        updatedJourney.setId(JOURNEY_ID);
        doNothing().when(journeyService).updateJourney(any(JourneyDto.class));

        ResponseEntity<Void> response = journeyController.updateJourney(updatedJourney);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(journeyService).updateJourney(any(JourneyDto.class));
    }

    @Test
    void updateJourney_withDifferentData_success() {
        JourneyDto updatedJourney = new JourneyDto();
        updatedJourney.setId(5L);
        updatedJourney.setGroupId(10L);
        doNothing().when(journeyService).updateJourney(any(JourneyDto.class));

        ResponseEntity<Void> response = journeyController.updateJourney(updatedJourney);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).updateJourney(updatedJourney);
    }

    @Test
    void addParticipationToJourney_success() {
        doNothing().when(journeyService).addParticipationToJourney(JOURNEY_ID, PARTICIPATION_ID);

        ResponseEntity<Void> response = journeyController.addParticipationToJourney(JOURNEY_ID, PARTICIPATION_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(journeyService).addParticipationToJourney(JOURNEY_ID, PARTICIPATION_ID);
    }

    @Test
    void addParticipationToJourney_withDifferentIds_success() {
        Long journeyId = 5L;
        Long participationId = 10L;
        doNothing().when(journeyService).addParticipationToJourney(journeyId, participationId);

        ResponseEntity<Void> response = journeyController.addParticipationToJourney(journeyId, participationId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).addParticipationToJourney(journeyId, participationId);
    }

    @Test
    void deleteJourney_success() {
        doNothing().when(journeyService).deleteJourney(JOURNEY_ID);

        ResponseEntity<Void> response = journeyController.deleteJourney(JOURNEY_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(journeyService).deleteJourney(JOURNEY_ID);
    }

    @Test
    void deleteJourney_withDifferentId_success() {
        Long journeyId = 100L;
        doNothing().when(journeyService).deleteJourney(journeyId);

        ResponseEntity<Void> response = journeyController.deleteJourney(journeyId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).deleteJourney(journeyId);
    }

    @Test
    void isUserParticipant_userIsParticipant_returnsTrue() {
        when(journeyService.isUserParticipantInJourney(JOURNEY_ID)).thenReturn(true);

        ResponseEntity<Boolean> response = journeyController.isUserParticipant(JOURNEY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
        verify(journeyService).isUserParticipantInJourney(JOURNEY_ID);
    }

    @Test
    void isUserParticipant_userIsNotParticipant_returnsFalse() {
        when(journeyService.isUserParticipantInJourney(JOURNEY_ID)).thenReturn(false);

        ResponseEntity<Boolean> response = journeyController.isUserParticipant(JOURNEY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody());
        verify(journeyService).isUserParticipantInJourney(JOURNEY_ID);
    }

    @Test
    void isUserParticipant_withDifferentJourneyId_success() {
        Long journeyId = 50L;
        when(journeyService.isUserParticipantInJourney(journeyId)).thenReturn(true);

        ResponseEntity<Boolean> response = journeyController.isUserParticipant(journeyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(journeyService).isUserParticipantInJourney(journeyId);
    }

    @Test
    void getAllParticipantsOfJourney_withParticipants_returnsSet() {
        Set<Long> participantIds = new HashSet<>();
        participantIds.add(1L);
        participantIds.add(2L);
        participantIds.add(3L);
        when(journeyService.getAllParticipantsOfJourney(JOURNEY_ID)).thenReturn(participantIds);

        ResponseEntity<Set<Long>> response = journeyController.getAllParticipantsOfJourney(JOURNEY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().contains(1L));
        assertTrue(response.getBody().contains(2L));
        assertTrue(response.getBody().contains(3L));
        verify(journeyService).getAllParticipantsOfJourney(JOURNEY_ID);
    }

    @Test
    void getAllParticipantsOfJourney_noParticipants_returnsEmptySet() {
        when(journeyService.getAllParticipantsOfJourney(JOURNEY_ID)).thenReturn(Collections.emptySet());

        ResponseEntity<Set<Long>> response = journeyController.getAllParticipantsOfJourney(JOURNEY_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(journeyService).getAllParticipantsOfJourney(JOURNEY_ID);
    }

    @Test
    void getAllParticipantsOfJourney_withDifferentJourneyId_success() {
        Long journeyId = 20L;
        Set<Long> participantIds = Set.of(10L, 20L);
        when(journeyService.getAllParticipantsOfJourney(journeyId)).thenReturn(participantIds);

        ResponseEntity<Set<Long>> response = journeyController.getAllParticipantsOfJourney(journeyId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(journeyService).getAllParticipantsOfJourney(journeyId);
    }

    @Test
    void changeJourneyStatus_success() {
        String status = "ACTIVE";
        doNothing().when(journeyService).changeJourneyStatus(JOURNEY_ID, status);

        ResponseEntity<Void> response = journeyController.changeJourneyStatus(JOURNEY_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(journeyService).changeJourneyStatus(JOURNEY_ID, status);
    }

    @Test
    void changeJourneyStatus_withInProgressStatus_success() {
        String status = "IN_PROGRESS";
        doNothing().when(journeyService).changeJourneyStatus(JOURNEY_ID, status);

        ResponseEntity<Void> response = journeyController.changeJourneyStatus(JOURNEY_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).changeJourneyStatus(JOURNEY_ID, status);
    }

    @Test
    void changeJourneyStatus_withFinishedStatus_success() {
        String status = "FINISHED";
        doNothing().when(journeyService).changeJourneyStatus(JOURNEY_ID, status);

        ResponseEntity<Void> response = journeyController.changeJourneyStatus(JOURNEY_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).changeJourneyStatus(JOURNEY_ID, status);
    }

    @Test
    void changeJourneyStatus_withCancelledStatus_success() {
        String status = "CANCELLED";
        doNothing().when(journeyService).changeJourneyStatus(JOURNEY_ID, status);

        ResponseEntity<Void> response = journeyController.changeJourneyStatus(JOURNEY_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).changeJourneyStatus(JOURNEY_ID, status);
    }

    @Test
    void changeJourneyStatus_withDifferentJourneyId_success() {
        Long journeyId = 99L;
        String status = "ACTIVE";
        doNothing().when(journeyService).changeJourneyStatus(journeyId, status);

        ResponseEntity<Void> response = journeyController.changeJourneyStatus(journeyId, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).changeJourneyStatus(journeyId, status);
    }

    @Test
    void changeJourneyStatus_withLowercaseStatus_success() {
        String status = "active";
        doNothing().when(journeyService).changeJourneyStatus(JOURNEY_ID, status);

        ResponseEntity<Void> response = journeyController.changeJourneyStatus(JOURNEY_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(journeyService).changeJourneyStatus(JOURNEY_ID, status);
    }
}
