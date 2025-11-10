package com.tfg.aegis.journey;

import com.tfg.aegis.group.GroupRepository;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.journey.mapper.JourneyMapper;
import com.tfg.aegis.journey.model.Enums;
import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.participation.ParticipationRepository;
import com.tfg.aegis.participation.mapper.ParticipationMapper;
import com.tfg.aegis.participation.model.Participation;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class JourneyService {
    private final JourneyRepository journeyRepository;
    private final JourneyMapper journeyMapper;
    private final ParticipationMapper participationMapper;
    private final ParticipationRepository participationRepository;
    private final GroupRepository groupRepository;

    /**
     * Retrieves a journey entity by its ID.
     *
     * @param id The ID of the journey entity.
     * @return The journey entity with the specified ID.
     */
    public JourneyDto getJourney(Long id) {
        return journeyRepository.findById(id)
                .map(journeyMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Journey with id %s not found".formatted(id)));
    }

    /**
     * Retrieves the current journey for a group.
     *
     * @param groupId The ID of the group.
     * @return The current journey entity for the specified user.
     */
    public JourneyDto getCurrentJourneyForGroup(Long groupId) {
        return journeyMapper.toDto(journeyRepository.findCurrentJourneyByGroupId(groupId));
    }

    /**
     * Retrieves all active journey entities.
     *
     * @return A list of all active journey entities.
     */
    public Set<JourneyDto> getActiveJourneys() {
        Set<Journey> journeys = (Set<Journey>)journeyRepository.findAll();
        Set<JourneyDto> journeyDtos = new HashSet<>();
        for (Journey journey : journeys) {
            if (journey.getState().equals(Enums.JourneyState.IN_PROGRESS)) {
                journeyDtos.add(journeyMapper.toDto(journey));
            }
        }
        return journeyDtos;
    }

    /**
     * Saves a journey entity to the database.
     *
     * @param journeyDto The journey entity to be saved.
     * @return The saved journey entity.
     */
    public Long createJourney(JourneyDto journeyDto) {
        // Map JourneyDto to Journey entity
        Journey journey = journeyMapper.toEntity(journeyDto);

        //Map ParticipationDto to Participation entity
        Set<Participation> participations = new HashSet<>();
        for (Long participationId : journeyDto.getParticipantsIds()) {
            Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation with id %s not found".formatted(participationId)));
            // en teoria si creas una participacion que ya exsite no deberia crear una nueva
            participations.add(participation);
        }
        journey.setParticipations(participations);

        if (journey.getState() == null) {
            journey.setState(Enums.JourneyState.PENDING);
        }

        Group group = groupRepository.findById(journeyDto.getGroupId())
            .orElseThrow(() -> new RuntimeException("Group with id %s not found".formatted(journeyDto.getGroupId())));

        journey.setGroup(group);

        journey = journeyRepository.save(journey);
        return journey.getId();
    }

    /**
     * Updates a journey entity in the database.
     *
     * @param journeyDto The journey entity with updated data.
     */
    public void updateJourney(JourneyDto journeyDto) {
        Journey updatedJourney = journeyMapper.toEntity(journeyDto);
        if (updatedJourney.getState().equals(Enums.JourneyState.COMPLETED)) {
            updatedJourney.setEndDate(java.time.LocalDateTime.now());
        }

        if (journeyDto.getGroupId() != null) {
            Group group = groupRepository.findById(journeyDto.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group with id %s not found".formatted(journeyDto.getGroupId())));
            updatedJourney.setGroup(group);
        }

        if (journeyDto.getParticipantsIds() != null) {
            Set<Participation> participations = new HashSet<>();
            for (Long participationId : journeyDto.getParticipantsIds()) {
                Participation participation = participationRepository.findById(participationId)
                    .orElseThrow(() -> new RuntimeException("Participation with id %s not found".formatted(participationId)));
                participations.add(participation);
            }
            updatedJourney.setParticipations(participations);
        }
        journeyRepository.save(updatedJourney);
    }

    /**
     * Deletes a journey entity from the database.
     *
     * @param id The ID of the journey entity to be deleted.
     */
    public void deleteJourney(Long id) {
        var journey = journeyRepository.findById(id).orElseThrow(() -> new RuntimeException("Journey with id %s not found".formatted(id)));
        journeyRepository.delete(journey);
    }

    // Cuando en un trayecto todas sus participaciones estén a "FINALIZADA", el trayecto pasará a estar en estado "FINALIZADO".
}
