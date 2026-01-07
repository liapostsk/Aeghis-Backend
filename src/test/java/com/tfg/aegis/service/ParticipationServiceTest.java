package com.tfg.aegis.service;

import com.tfg.aegis.model.dto.ParticipationDto;
import com.tfg.aegis.model.entity.*;
import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.enums.JourneyEnums;
import com.tfg.aegis.model.enums.ParticipationEnums;
import com.tfg.aegis.model.mapper.ParticipationMapper;
import com.tfg.aegis.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private ParticipationRepository participationRepository;
    @Mock
    private ParticipationMapper participationMapper;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private ParticipationService service;

    /* =========================
     * getParticipation
     * ========================= */
    @Test
    void getParticipation_whenExists_returnsDto() {
        Long id = 1L;
        Participation participation = new Participation();
        participation.setId(id);

        ParticipationDto dto = new ParticipationDto();
        dto.setId(id);

        when(participationRepository.findById(id)).thenReturn(Optional.of(participation));
        when(participationMapper.toDto(participation)).thenReturn(dto);

        ParticipationDto result = service.getParticipation(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(participationRepository).findById(id);
        verify(participationMapper).toDto(participation);
    }

    @Test
    void getParticipation_whenNotExists_throwsRuntimeException() {
        Long id = 999L;
        when(participationRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getParticipation(id));

        assertTrue(ex.getMessage().contains("Participation not found"));
    }

    /* =========================
     * createParticipation
     * ========================= */
    @Test
    void createParticipation_withDifferentLocations_setsAcceptedState() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(2L);
        dto.setJourneyId(10L);
        dto.setUserId(5L);

        Location source = new Location();
        source.setId(1L);
        Location destination = new Location();
        destination.setId(2L);
        Journey journey = new Journey();
        journey.setId(10L);
        User user = new User();
        user.setId(5L);

        Participation entity = new Participation();
        entity.setState(null); // Estado null al inicio

        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(participationMapper.toEntity(dto)).thenReturn(entity);
        when(participationRepository.existsByJourneyAndParticipant(journey, user)).thenReturn(false);

        Participation saved = new Participation();
        saved.setId(100L);
        saved.setState(ParticipationEnums.ParticipationState.ACCEPTED);
        when(participationRepository.save(any(Participation.class))).thenReturn(saved);

        Long result = service.createParticipation(dto);

        assertEquals(100L, result);
        assertEquals(ParticipationEnums.ParticipationState.ACCEPTED, entity.getState());
        assertEquals(journey, entity.getJourney());
        assertEquals(user, entity.getParticipant());
        assertEquals(source, entity.getSource());
        assertEquals(destination, entity.getDestination());
        verify(participationRepository).save(entity);
    }

    @Test
    void createParticipation_withSameSourceAndDestination_setsArrivedState() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(1L); // Mismo ID
        dto.setJourneyId(10L);
        dto.setUserId(5L);

        Location sameLocation = new Location();
        sameLocation.setId(1L);
        Journey journey = new Journey();
        journey.setId(10L);
        journey.setParticipation(new HashSet<>());
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        User user = new User();
        user.setId(5L);

        Participation entity = new Participation();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(sameLocation));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(participationMapper.toEntity(dto)).thenReturn(entity);
        when(participationRepository.existsByJourneyAndParticipant(journey, user)).thenReturn(false);

        Participation saved = new Participation();
        saved.setId(100L);
        saved.setState(ParticipationEnums.ParticipationState.ARRIVED);
        saved.setJourney(journey);
        saved.setParticipant(user);
        when(participationRepository.save(any(Participation.class))).thenReturn(saved);
        when(participationRepository.findByJourney_IdAndParticipant_Id(10L, 5L))
                .thenReturn(Optional.of(saved));

        Long result = service.createParticipation(dto);

        assertEquals(100L, result);
        assertEquals(ParticipationEnums.ParticipationState.ARRIVED, entity.getState());
        assertNotNull(entity.getArrivalTime());
        verify(participationRepository).save(entity);
    }

    @Test
    void createParticipation_withExistingState_keepsState() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(2L);
        dto.setJourneyId(10L);
        dto.setUserId(5L);

        Location source = new Location();
        source.setId(1L);
        Location destination = new Location();
        destination.setId(2L);
        Journey journey = new Journey();
        journey.setId(10L);
        User user = new User();
        user.setId(5L);

        Participation entity = new Participation();
        entity.setState(ParticipationEnums.ParticipationState.CANCELLED); // Ya tiene estado

        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(participationMapper.toEntity(dto)).thenReturn(entity);
        when(participationRepository.existsByJourneyAndParticipant(journey, user)).thenReturn(false);

        Participation saved = new Participation();
        saved.setId(100L);
        when(participationRepository.save(any(Participation.class))).thenReturn(saved);

        Long result = service.createParticipation(dto);

        assertEquals(100L, result);
        assertEquals(ParticipationEnums.ParticipationState.CANCELLED, entity.getState());
    }

    @Test
    void createParticipation_sourceNotFound_throwsRuntimeException() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(999L);

        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createParticipation(dto));

        assertTrue(ex.getMessage().contains("Source location not found"));
    }

    @Test
    void createParticipation_destinationNotFound_throwsRuntimeException() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(999L);

        Location source = new Location();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createParticipation(dto));

        assertTrue(ex.getMessage().contains("Destination location not found"));
    }

    @Test
    void createParticipation_journeyNotFound_throwsRuntimeException() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(2L);
        dto.setJourneyId(999L);

        Location source = new Location();
        Location destination = new Location();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(journeyRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createParticipation(dto));

        assertTrue(ex.getMessage().contains("Journey not found"));
    }

    @Test
    void createParticipation_userNotFound_throwsRuntimeException() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(2L);
        dto.setJourneyId(10L);
        dto.setUserId(999L);

        Location source = new Location();
        Location destination = new Location();
        Journey journey = new Journey();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createParticipation(dto));

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void createParticipation_alreadyExists_throwsRuntimeException() {
        ParticipationDto dto = new ParticipationDto();
        dto.setSourceId(1L);
        dto.setDestinationId(2L);
        dto.setJourneyId(10L);
        dto.setUserId(5L);

        Location source = new Location();
        source.setId(1L);
        Location destination = new Location();
        destination.setId(2L);
        Journey journey = new Journey();
        User user = new User();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(participationMapper.toEntity(dto)).thenReturn(new Participation());
        when(participationRepository.existsByJourneyAndParticipant(journey, user)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createParticipation(dto));

        assertTrue(ex.getMessage().contains("Participation already exists"));
        verify(participationRepository, never()).save(any());
    }

    /* =========================
     * updateParticipation
     * ========================= */
    @Test
    void updateParticipation_toArrivedState_setsArrivalTime() {
        ParticipationDto dto = new ParticipationDto();
        dto.setId(1L);
        dto.setState(ParticipationEnums.ParticipationState.ARRIVED);

        Participation existing = new Participation();
        existing.setId(1L);
        existing.setArrivalTime(null);

        Journey journey = new Journey();
        journey.setId(10L);
        journey.setParticipation(new HashSet<>());
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        existing.setJourney(journey);

        User user = new User();
        user.setId(5L);
        existing.setParticipant(user);

        when(participationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(participationRepository.save(existing)).thenReturn(existing);
        when(participationRepository.findByJourney_IdAndParticipant_Id(10L, 5L))
                .thenReturn(Optional.of(existing));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));

        service.updateParticipation(dto);

        assertEquals(ParticipationEnums.ParticipationState.ARRIVED, existing.getState());
        assertNotNull(existing.getArrivalTime());
        verify(participationRepository).save(existing);
    }

    @Test
    void updateParticipation_alreadyArrived_doesNotUpdateArrivalTime() {
        ParticipationDto dto = new ParticipationDto();
        dto.setId(1L);
        dto.setState(ParticipationEnums.ParticipationState.ARRIVED);

        LocalDateTime originalArrival = LocalDateTime.now().minusHours(1);

        Participation existing = new Participation();
        existing.setId(1L);
        existing.setArrivalTime(originalArrival); // Ya tiene arrival time

        Journey journey = new Journey();
        journey.setId(10L);
        journey.setParticipation(new HashSet<>());
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        existing.setJourney(journey);

        User user = new User();
        user.setId(5L);
        existing.setParticipant(user);

        when(participationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(participationRepository.save(existing)).thenReturn(existing);
        when(participationRepository.findByJourney_IdAndParticipant_Id(10L, 5L))
                .thenReturn(Optional.of(existing));
        when(journeyRepository.findById(10L)).thenReturn(Optional.of(journey));

        service.updateParticipation(dto);

        assertEquals(originalArrival, existing.getArrivalTime()); // No cambió
    }

    @Test
    void updateParticipation_stateNull_doesNotUpdate() {
        ParticipationDto dto = new ParticipationDto();
        dto.setId(1L);
        dto.setState(null); // Sin estado

        Participation existing = new Participation();
        existing.setId(1L);
        existing.setState(ParticipationEnums.ParticipationState.ACCEPTED);

        when(participationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(participationRepository.save(existing)).thenReturn(existing);

        service.updateParticipation(dto);

        assertEquals(ParticipationEnums.ParticipationState.ACCEPTED, existing.getState());
        verify(participationRepository).save(existing);
    }

    @Test
    void updateParticipation_notFound_throwsRuntimeException() {
        ParticipationDto dto = new ParticipationDto();
        dto.setId(999L);

        when(participationRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateParticipation(dto));

        assertTrue(ex.getMessage().contains("Participation not found"));
    }

    /* =========================
     * markArrived
     * ========================= */
    @Test
    void markArrived_notYetArrived_updatesStateAndChecksJourneyCompletion() {
        Long journeyId = 10L;
        Long userId = 5L;

        Participation p = new Participation();
        p.setId(1L);
        p.setState(ParticipationEnums.ParticipationState.ACCEPTED);
        p.setArrivalTime(null);

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        journey.setParticipation(Set.of(p));
        p.setJourney(journey);

        User user = new User();
        user.setId(userId);
        p.setParticipant(user);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.of(p));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.markArrived(journeyId, userId);

        assertEquals(ParticipationEnums.ParticipationState.ARRIVED, p.getState());
        assertNotNull(p.getArrivalTime());
        verify(participationRepository).save(p);

        // Journey debe completarse porque solo hay 1 participación y está ARRIVED
        assertEquals(JourneyEnums.JourneyState.COMPLETED, journey.getState());
        assertNotNull(journey.getEndDate());
        verify(journeyRepository).save(journey);
    }

    @Test
    void markArrived_alreadyArrived_doesNotUpdateAgain() {
        Long journeyId = 10L;
        Long userId = 5L;

        Participation p = new Participation();
        p.setId(1L);
        p.setState(ParticipationEnums.ParticipationState.ARRIVED); // Ya arrived
        p.setArrivalTime(LocalDateTime.now().minusMinutes(30));

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);
        journey.setParticipation(Set.of(p));

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        p.setJourney(journey);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.of(p));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.markArrived(journeyId, userId);

        // No debería guardar de nuevo la participación
        verify(participationRepository, never()).save(p);

        // Pero sí debe completar el journey
        verify(journeyRepository).save(journey);
    }

    @Test
    void markArrived_allParticipantsArrived_completesJourney() {
        Long journeyId = 10L;
        Long userId1 = 5L;

        User user1 = new User(); user1.setId(userId1);
        User user2 = new User(); user2.setId(6L);

        Participation p1 = new Participation();
        p1.setId(1L);
        p1.setState(ParticipationEnums.ParticipationState.ACCEPTED);
        p1.setParticipant(user1);

        Participation p2 = new Participation();
        p2.setId(2L);
        p2.setState(ParticipationEnums.ParticipationState.ARRIVED);
        p2.setParticipant(user2);

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);
        journey.setParticipation(Set.of(p1, p2));

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        p1.setJourney(journey);
        p2.setJourney(journey);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId1))
                .thenReturn(Optional.of(p1));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.markArrived(journeyId, userId1);

        // Ahora ambas están ARRIVED, journey debe completarse
        assertEquals(JourneyEnums.JourneyState.COMPLETED, journey.getState());
        assertNotNull(journey.getEndDate());
        verify(journeyRepository).save(journey);
    }

    @Test
    void markArrived_temporalGroupAllArrived_closesGroup() {
        Long journeyId = 10L;
        Long userId = 5L;

        Participation p = new Participation();
        p.setId(1L);
        p.setState(ParticipationEnums.ParticipationState.ACCEPTED);

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);
        journey.setParticipation(Set.of(p));

        Group group = new Group();
        group.setId(100L);
        group.setType(GroupEnums.TypeGroup.TEMPORAL);
        group.setState(GroupEnums.GroupState.ACTIVO);
        journey.setGroup(group);

        p.setJourney(journey);

        User user = new User();
        user.setId(userId);
        p.setParticipant(user);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.of(p));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.markArrived(journeyId, userId);

        // Grupo temporal debe cerrarse
        assertEquals(GroupEnums.GroupState.CERRADO, group.getState());
        assertNotNull(group.getLastModified());
        assertNotNull(group.getExpirationDate());
        verify(groupRepository).save(group);
    }

    @Test
    void markArrived_withCancelledParticipation_stillCompletesJourney() {
        Long journeyId = 10L;
        Long userId = 5L;

        User user1 = new User(); user1.setId(userId);
        User user2 = new User(); user2.setId(6L);

        Participation p1 = new Participation();
        p1.setId(1L);
        p1.setState(ParticipationEnums.ParticipationState.ACCEPTED);
        p1.setParticipant(user1);

        Participation p2 = new Participation();
        p2.setId(2L);
        p2.setState(ParticipationEnums.ParticipationState.CANCELLED); // Cancelada
        p2.setParticipant(user2);

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.IN_PROGRESS);
        journey.setParticipation(Set.of(p1, p2));

        Group group = new Group();
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        journey.setGroup(group);

        p1.setJourney(journey);
        p2.setJourney(journey);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.of(p1));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.markArrived(journeyId, userId);

        // p1 ARRIVED + p2 CANCELLED = todas finalizadas → completa journey
        assertEquals(JourneyEnums.JourneyState.COMPLETED, journey.getState());
        verify(journeyRepository).save(journey);
    }

    @Test
    void markArrived_journeyAlreadyCompleted_doesNotUpdateAgain() {
        Long journeyId = 10L;
        Long userId = 5L;

        Participation p = new Participation();
        p.setId(1L);
        p.setState(ParticipationEnums.ParticipationState.ARRIVED);
        p.setArrivalTime(LocalDateTime.now());

        Journey journey = new Journey();
        journey.setId(journeyId);
        journey.setState(JourneyEnums.JourneyState.COMPLETED); // Ya completado
        journey.setParticipation(Set.of(p));
        p.setJourney(journey);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.of(p));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.of(journey));

        service.markArrived(journeyId, userId);

        // Journey sigue COMPLETED, no cambia
        assertEquals(JourneyEnums.JourneyState.COMPLETED, journey.getState());
        verify(participationRepository, never()).save(p);
        verify(journeyRepository, never()).save(journey);
    }

    @Test
    void markArrived_participationNotFound_throwsRuntimeException() {
        Long journeyId = 10L;
        Long userId = 999L;

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.markArrived(journeyId, userId));

        assertTrue(ex.getMessage().contains("Participation not found"));
    }

    @Test
    void markArrived_journeyNotFound_throwsRuntimeException() {
        Long journeyId = 999L;
        Long userId = 5L;

        Participation p = new Participation();
        p.setState(ParticipationEnums.ParticipationState.ACCEPTED);

        when(participationRepository.findByJourney_IdAndParticipant_Id(journeyId, userId))
                .thenReturn(Optional.of(p));
        when(journeyRepository.findById(journeyId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.markArrived(journeyId, userId));

        assertTrue(ex.getMessage().contains("Journey not found"));
    }
}
