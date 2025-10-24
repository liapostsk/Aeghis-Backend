package com.tfg.aegis.journey;

import com.tfg.aegis.journey.mapper.JourneyMapper;
import com.tfg.aegis.journey.model.Enums;
import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.participation.ParticipationService;
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
    private final ParticipationService participationService;

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
            if (journey.getState().equals(Enums.EstadoTrayecto.ACTIVO)) {
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
        for (Long participationId : journeyDto.getParticipationIds()) {
            Participation participation = participationMapper.toEntity(participationService.getParticipation(participationId));
            participations.add(participation);
        }
        journey.setParticipations(participations);

        if (journey.getState() == null) {
            journey.setState(Enums.EstadoTrayecto.PENDIENTE);
        }

        // En caso de que el trayecto sea comun a todos. <-- REVISAR IF (TIPO DE TRAYECTO)
        if (journey.getDestino() != null && journey.getSourcePoint() != null) {
            Location destino = journey.getDestino();
            journey.setDestino(destino);

            Location sourcePoint = journey.getSourcePoint();
            journey.setSourcePoint(sourcePoint);
        }

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
        if (updatedJourney.getState().equals(Enums.EstadoTrayecto.ACABADO)) {
            updatedJourney.setEndDate(java.time.LocalDateTime.now());
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
