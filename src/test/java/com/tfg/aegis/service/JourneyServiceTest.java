package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.model.dto.JourneyDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.entity.Journey;
import com.tfg.aegis.model.entity.Participation;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.enums.JourneyEnums;
import com.tfg.aegis.model.mapper.JourneyMapper;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.repository.JourneyRepository;
import com.tfg.aegis.repository.ParticipationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JourneyServiceTest {

    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private JourneyMapper journeyMapper;
    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private JourneyService service;

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    /* =========================
     * getJourney
     * ========================= */
    @Test
    void getJourney_whenExists_returnsDto() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);

        JourneyDto dto = new JourneyDto();
        dto.setId(journeyId);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        when(journeyMapper.toDto(journey)).thenReturn(dto);

        JourneyDto result = service.getJourney(journeyId);

        assertNotNull(result);
        assertEquals(journeyId, result.getId());
        verify(journeyRepository).findById(journeyId);
        verify(journeyMapper).toDto(journey);
    }

    @Test
    void getJourney_whenNotExists_throwsRuntimeException() {
        Long journeyId = 999L;
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getJourney(journeyId));

        assertTrue(ex.getMessage().contains("Journey with id"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    /* =========================
     * getCurrentJourneyForGroup
     * ========================= */
    @Test
    void getCurrentJourneyForGroup_whenInProgressExists_returnsInProgress() {
        Long groupId = 1L;
        Group group = new Group();
        group.setId(groupId);

        Journey journey = new Journey();
        journey.setId(10L);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        JourneyDto dto = new JourneyDto();
        dto.setId(10L);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(journeyRepository.findByGroupAndState(group, JourneyEnums.JourneyState.IN_PROGRESS))
                .thenReturn(journey);
        when(journeyMapper.toDto(journey)).thenReturn(dto);

        JourneyDto result = service.getCurrentJourneyForGroup(groupId);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(journeyRepository).findByGroupAndState(group, JourneyEnums.JourneyState.IN_PROGRESS);
        verify(journeyRepository, never()).findByGroupAndState(group, JourneyEnums.JourneyState.PENDING);
    }

    @Test
    void getCurrentJourneyForGroup_whenNoInProgress_returnsPending() {
        Long groupId = 1L;
        Group group = new Group();
        group.setId(groupId);

        Journey pendingJourney = new Journey();
        pendingJourney.setId(20L);
        pendingJourney.setState(JourneyEnums.JourneyState.PENDING);

        JourneyDto dto = new JourneyDto();
        dto.setId(20L);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(journeyRepository.findByGroupAndState(group, JourneyEnums.JourneyState.IN_PROGRESS))
                .thenReturn(null);
        when(journeyRepository.findByGroupAndState(group, JourneyEnums.JourneyState.PENDING))
                .thenReturn(pendingJourney);
        when(journeyMapper.toDto(pendingJourney)).thenReturn(dto);

        JourneyDto result = service.getCurrentJourneyForGroup(groupId);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        verify(journeyRepository).findByGroupAndState(group, JourneyEnums.JourneyState.IN_PROGRESS);
        verify(journeyRepository).findByGroupAndState(group, JourneyEnums.JourneyState.PENDING);
    }

    @Test
    void getCurrentJourneyForGroup_whenNoCurrentJourney_throwsRuntimeException() {
        Long groupId = 1L;
        Group group = new Group();
        group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(journeyRepository.findByGroupAndState(group, JourneyEnums.JourneyState.IN_PROGRESS))
                .thenReturn(null);
        when(journeyRepository.findByGroupAndState(group, JourneyEnums.JourneyState.PENDING))
                .thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getCurrentJourneyForGroup(groupId));

        assertTrue(ex.getMessage().contains("No current journey found for group"));
    }

    @Test
    void getCurrentJourneyForGroup_whenGroupNotFound_throwsRuntimeException() {
        Long groupId = 999L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getCurrentJourneyForGroup(groupId));

        assertTrue(ex.getMessage().contains("Group with id"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    /* =========================
     * getActiveJourneys
     * ========================= */
    @Test
    void getActiveJourneys_returnsInProgressJourneys() {
        Journey j1 = new Journey();
        j1.setId(1L);
        Journey j2 = new Journey();
        j2.setId(2L);

        JourneyDto dto1 = new JourneyDto();
        dto1.setId(1L);
        JourneyDto dto2 = new JourneyDto();
        dto2.setId(2L);

        when(journeyRepository.findByState(JourneyEnums.JourneyState.IN_PROGRESS))
                .thenReturn(Arrays.asList(j1, j2));
        when(journeyMapper.toDto(j1)).thenReturn(dto1);
        when(journeyMapper.toDto(j2)).thenReturn(dto2);

        Set<JourneyDto> result = service.getActiveJourneys();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(journeyRepository).findByState(JourneyEnums.JourneyState.IN_PROGRESS);
    }

    @Test
    void getActiveJourneys_whenNoneActive_returnsEmptySet() {
        when(journeyRepository.findByState(JourneyEnums.JourneyState.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        Set<JourneyDto> result = service.getActiveJourneys();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /* =========================
     * createJourney
     * ========================= */
    @Test
    void createJourney_withStateNull_setsPending() {
        JourneyDto dto = new JourneyDto();
        dto.setGroupId(1L);

        Journey journey = new Journey();
        journey.setState(null);

        Group group = new Group();
        group.setId(1L);

        Journey savedJourney = new Journey();
        savedJourney.setId(10L);

        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(journeyRepository.save(any(Journey.class))).thenReturn(savedJourney);

        Long result = service.createJourney(dto);

        assertEquals(10L, result);
        assertEquals(JourneyEnums.JourneyState.PENDING, journey.getState());
        assertNotNull(journey.getParticipation());
        assertEquals(group, journey.getGroup());
        verify(journeyRepository).save(journey);
    }

    @Test
    void createJourney_withExistingState_keepsState() {
        JourneyDto dto = new JourneyDto();
        dto.setGroupId(1L);

        Journey journey = new Journey();
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setId(1L);

        Journey savedJourney = new Journey();
        savedJourney.setId(10L);

        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(journeyRepository.save(any(Journey.class))).thenReturn(savedJourney);

        Long result = service.createJourney(dto);

        assertEquals(10L, result);
        assertEquals(JourneyEnums.JourneyState.IN_PROGRESS, journey.getState());
    }

    @Test
    void createJourney_groupNotFound_throwsRuntimeException() {
        JourneyDto dto = new JourneyDto();
        dto.setGroupId(999L);

        Journey journey = new Journey();
        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createJourney(dto));

        assertTrue(ex.getMessage().contains("Group with id"));
        verify(journeyRepository, never()).save(any());
    }

    /* =========================
     * updateJourney
     * ========================= */
    @Test
    void updateJourney_withCompletedState_setsEndDate() {
        JourneyDto dto = new JourneyDto();
        dto.setId(1L);
        dto.setGroupId(1L);

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setState(JourneyEnums.JourneyState.COMPLETED);

        Group group = new Group();
        group.setId(1L);

        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        service.updateJourney(dto);

        assertNotNull(journey.getEndDate());
        verify(journeyRepository).save(journey);
    }

    @Test
    void updateJourney_withParticipantsIds_setsParticipations() {
        JourneyDto dto = new JourneyDto();
        dto.setId(1L);
        dto.setGroupId(1L);
        dto.setParticipantsIds(Set.of(10L, 20L));

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setId(1L);

        Participation p1 = new Participation();
        p1.setId(10L);
        Participation p2 = new Participation();
        p2.setId(20L);

        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(participationRepository.findById(10L)).thenReturn(Optional.of(p1));
        when(participationRepository.findById(20L)).thenReturn(Optional.of(p2));

        service.updateJourney(dto);

        assertNotNull(journey.getParticipation());
        assertEquals(2, journey.getParticipation().size());
        verify(journeyRepository).save(journey);
    }

    @Test
    void updateJourney_participationNotFound_throwsRuntimeException() {
        JourneyDto dto = new JourneyDto();
        dto.setId(1L);
        dto.setGroupId(1L);
        dto.setParticipantsIds(Set.of(999L));

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setId(1L);

        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(participationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateJourney(dto));

        assertTrue(ex.getMessage().contains("Participation with id"));
        verify(journeyRepository, never()).save(any());
    }

    @Test
    void updateJourney_groupNotFound_throwsRuntimeException() {
        JourneyDto dto = new JourneyDto();
        dto.setId(1L);
        dto.setGroupId(999L);

        Journey journey = new Journey();
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS); // Necesita un estado válido

        when(journeyMapper.toEntity(dto)).thenReturn(journey);
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateJourney(dto));

        assertTrue(ex.getMessage().contains("Group with id"));
    }

    @Test
    void updateJourney_withoutGroupIdAndParticipants_updatesBasicFields() {
        JourneyDto dto = new JourneyDto();
        dto.setId(1L);
        dto.setGroupId(null);
        dto.setParticipantsIds(null);

        Journey journey = new Journey();
        journey.setId(1L);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        when(journeyMapper.toEntity(dto)).thenReturn(journey);

        service.updateJourney(dto);

        verify(journeyRepository).save(journey);
        verify(groupRepository, never()).findById(any());
        verify(participationRepository, never()).findById(any());
    }

    /* =========================
     * deleteJourney
     * ========================= */
    @Test
    void deleteJourney_whenExists_deletesSuccessfully() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.deleteJourney(journeyId);

        verify(journeyRepository).findById(journeyId);
        verify(journeyRepository).delete(journey);
    }

    @Test
    void deleteJourney_whenNotExists_throwsRuntimeException() {
        Long journeyId = 999L;
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.deleteJourney(journeyId));

        assertTrue(ex.getMessage().contains("Journey with id"));
        verify(journeyRepository, never()).delete(any());
    }

    /* =========================
     * addParticipationToJourney
     * ========================= */
    @Test
    void addParticipationToJourney_ok_addsSuccessfully() {
        Long journeyId = 1L;
        Long participationId = 10L;

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setParticipation(new HashSet<>());

        Participation participation = new Participation();
        participation.setId(participationId);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        when(participationRepository.findById(participationId)).thenReturn(Optional.of(participation));

        service.addParticipationToJourney(journeyId, participationId);

        assertTrue(journey.getParticipation().contains(participation));
        verify(journeyRepository).save(journey);
    }

    @Test
    void addParticipationToJourney_journeyNotFound_throwsRuntimeException() {
        Long journeyId = 999L;
        Long participationId = 10L;

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addParticipationToJourney(journeyId, participationId));

        assertTrue(ex.getMessage().contains("Journey with id"));
        verify(journeyRepository, never()).save(any());
    }

    @Test
    void addParticipationToJourney_participationNotFound_throwsRuntimeException() {
        Long journeyId = 1L;
        Long participationId = 999L;

        Journey journey = new Journey();
        journey.setId(journeyId);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        when(participationRepository.findById(participationId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.addParticipationToJourney(journeyId, participationId));

        assertTrue(ex.getMessage().contains("Participation with id"));
        verify(journeyRepository, never()).save(any());
    }

    /* =========================
     * isUserParticipantInJourney
     * ========================= */
    @Test
    void isUserParticipantInJourney_whenIsParticipant_returnsTrue() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_123");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);

        UserDto user = new UserDto();
        user.setId(5L);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        when(userService.getUserByClerkId("clerk_123")).thenReturn(user);
        when(participationRepository.existsByJourney_IdAndParticipant_Id(journeyId, 5L))
                .thenReturn(true);

        boolean result = service.isUserParticipantInJourney(journeyId);

        assertTrue(result);
    }

    @Test
    void isUserParticipantInJourney_whenNotParticipant_returnsFalse() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_456");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);

        UserDto user = new UserDto();
        user.setId(7L);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));
        when(userService.getUserByClerkId("clerk_456")).thenReturn(user);
        when(participationRepository.existsByJourney_IdAndParticipant_Id(journeyId, 7L))
                .thenReturn(false);

        boolean result = service.isUserParticipantInJourney(journeyId);

        assertFalse(result);
    }

    @Test
    void isUserParticipantInJourney_journeyNotFound_throwsRuntimeException() {
        Long journeyId = 999L;
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.isUserParticipantInJourney(journeyId));

        assertTrue(ex.getMessage().contains("Journey with id"));
    }

    /* =========================
     * getAllParticipantsOfJourney
     * ========================= */
    @Test
    void getAllParticipantsOfJourney_returnsParticipantIds() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);

        User u1 = new User();
        u1.setId(10L);
        User u2 = new User();
        u2.setId(20L);

        Participation p1 = new Participation();
        p1.setParticipant(u1);
        Participation p2 = new Participation();
        p2.setParticipant(u2);

        journey.setParticipation(new HashSet<>(Arrays.asList(p1, p2)));

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        Set<Long> result = service.getAllParticipantsOfJourney(journeyId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(20L));
    }

    @Test
    void getAllParticipantsOfJourney_whenParticipationsNull_returnsEmptySet() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setParticipation(null);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        Set<Long> result = service.getAllParticipantsOfJourney(journeyId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllParticipantsOfJourney_journeyNotFound_throwsNotFoundException() {
        Long journeyId = 999L;
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getAllParticipantsOfJourney(journeyId));
    }

    /* =========================
     * changeJourneyStatus
     * ========================= */
    @Test
    void changeJourneyStatus_validStatus_updatesSuccessfully() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.PENDING);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.changeJourneyStatus(journeyId, "IN_PROGRESS");

        assertEquals(JourneyEnums.JourneyState.IN_PROGRESS, journey.getState());
        verify(journeyRepository).save(journey);
    }

    @Test
    void changeJourneyStatus_toCompleted_updatesSuccessfully() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.changeJourneyStatus(journeyId, "COMPLETED");

        assertEquals(JourneyEnums.JourneyState.COMPLETED, journey.getState());
        verify(journeyRepository).save(journey);
    }

    @Test
    void changeJourneyStatus_invalidStatus_throwsRuntimeException() {
        Long journeyId = 1L;
        Journey journey = new Journey();
        journey.setId(journeyId);

        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.changeJourneyStatus(journeyId, "INVALID_STATUS"));

        assertTrue(ex.getMessage().contains("Invalid journey state"));
        verify(journeyRepository, never()).save(any());
    }

    @Test
    void changeJourneyStatus_journeyNotFound_throwsNotFoundException() {
        Long journeyId = 999L;
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.changeJourneyStatus(journeyId, "IN_PROGRESS"));

        verify(journeyRepository, never()).save(any());
    }
}
