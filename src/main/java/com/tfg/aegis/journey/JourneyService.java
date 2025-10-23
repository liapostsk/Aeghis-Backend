package com.tfg.aegis.journey;

import com.tfg.aegis.journey.mapper.JourneyMapper;
import com.tfg.aegis.journey.model.Enums;
import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.participacion.mapper.ParticipationMapper;
import com.tfg.aegis.participacion.model.Participation;
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
     * Retrieves the current journey for a user.
     *
     * @param groupId The ID of the user.
     * @return The current journey entity for the specified user.
     */
    public JourneyDto getCurrentJourney(Long groupId) {
        return journeyMapper.toDto(journeyRepository.findCurrentJourneyByGroupId(groupId));
    }

    /**
     * Retrieves all active journey entities.
     *
     * @return A list of all active journey entities.
     */
    public java.util.List<JourneyDto> getActiveJourneys() {
        java.util.List<Journey> journeys = (java.util.List<Journey>) journeyRepository.findAll();
        java.util.List<JourneyDto> journeyDtos = new java.util.ArrayList<>();
        for (Journey journey : journeys) {
            if (journey.getEstado().equals(Enums.EstadoTrayecto.ACTIVO)) {
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
    public Long createJourney(JourneyDto journeyDto, Set<Long> participacionDtoSet) {
        // Map JourneyDto to Journey entity
        Journey journey = journeyMapper.toEntity(journeyDto);
        //Map ParticipationDto to Participation entity
        Set<Participation> participations = new HashSet<>();
        for (Long participationId : participacionDtoSet) {
            Participation participation = new Participation();
            participations.add(participation);
        }
        journey.setParticipations(participations);

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
}
