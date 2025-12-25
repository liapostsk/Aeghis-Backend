package com.tfg.aegis.service;

import com.tfg.aegis.model.enums.JourneyEnums;
import com.tfg.aegis.repository.JourneyRepository;
import com.tfg.aegis.model.entity.Journey;
import com.tfg.aegis.repository.LocationRepository;
import com.tfg.aegis.model.entity.Location;
import com.tfg.aegis.model.mapper.ParticipationMapper;
import com.tfg.aegis.model.enums.ParticipationEnums;
import com.tfg.aegis.model.entity.Participation;
import com.tfg.aegis.model.dto.ParticipationDto;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.repository.ParticipationRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@AllArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final ParticipationMapper participationMapper;
    private final LocationRepository locationRepository;
    private final JourneyRepository journeyRepository;
    private final UserRepository userRepository;

    /**
     * Get a participation by its ID.
     * @param id the ID of the participation
     * @return the ParticipationDto object
     */
    public ParticipationDto getParticipation(Long id) {
        Participation participation = participationRepository.findById(id).orElseThrow(() -> new RuntimeException("Participation not found"));
        return participationMapper.toDto(participation);
    }

    /**
     * Create a new participation.
     * @param participationDto the ParticipationDto object
     * @return the ID of the created participation
     */
    public Long createParticipation(ParticipationDto participationDto) {
        Location source = locationRepository.findById(participationDto.getSourceId()).orElseThrow(() -> new RuntimeException("Source location not found"));
        Location destination = locationRepository.findById(participationDto.getDestinationId()).orElseThrow(() -> new RuntimeException("Destination location not found"));
        Journey journey = journeyRepository.findById(participationDto.getJourneyId()).orElseThrow(() -> new RuntimeException("Journey not found"));
        User user = userRepository.findById(participationDto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

        Participation participation = participationMapper.toEntity(participationDto);
        participation.setJourney(journey);
        participation.setParticipant(user);
        participation.setSource(source);
        participation.setDestination(destination);

        if (source.getId() != null && source.getId().equals(destination.getId())) {
            participation.setState(ParticipationEnums.ParticipationState.ARRIVED);
            participation.setArrivalTime(LocalDateTime.now());
        } else if (participation.getState() == null) {
            participation.setState(ParticipationEnums.ParticipationState.ACCEPTED);
        }

        // si ya hay una participacion con ese journey y user, lanzar excepcion
        if (participationRepository.existsByJourneyAndParticipant(journey, user)) {
            throw new RuntimeException("Participation already exists for this journey and user");
        }
        Participation savedParticipation = participationRepository.save(participation);

        return savedParticipation.getId();
    }

    /**
     * Update an existing participation.
     * @param participationDto the ParticipationDto object
     */
    public void updateParticipation(ParticipationDto participationDto) {
        Long id = participationDto.getId();
        Participation existing = participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation not found: " + id));

        if (participationDto.getState() != null) {
            existing.setState(participationDto.getState());

            if (participationDto.getState() == ParticipationEnums.ParticipationState.ARRIVED
                    && existing.getArrivalTime() == null) {
                existing.setArrivalTime(LocalDateTime.now());
            }
        }

        Participation saved = participationRepository.save(existing);

        if (participationDto.getState() == ParticipationEnums.ParticipationState.ARRIVED) {
            markArrived(saved.getJourney().getId(), saved.getParticipant().getId());
        }
    }

    public void markArrived(Long journeyId, Long userId) {

        // 1) Encontrar la participación (journey + user)
        Participation p = participationRepository
                .findByJourney_IdAndParticipant_Id(journeyId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Participation not found for journey " + journeyId + " and user " + userId
                ));

        // 2) Si ya estaba arrived, no hacemos nada (idempotente)
        if (p.getState() == ParticipationEnums.ParticipationState.ARRIVED) {
            return;
        }

        // 3) Marcar ARRIVED y arrivalTime
        p.setState(ParticipationEnums.ParticipationState.ARRIVED);
        p.setArrivalTime(LocalDateTime.now());
        participationRepository.save(p);

        // 4) Revisar si el trayecto puede completarse
        Journey journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new RuntimeException("Journey not found: " + journeyId));

        // OJO: decide tu regla. Aquí: COMPLETED si todos están ARRIVED o CANCELLED
        boolean allDone = journey.getParticipations().stream().allMatch(part ->
                part.getState() == ParticipationEnums.ParticipationState.ARRIVED
                        || part.getState() == ParticipationEnums.ParticipationState.CANCELLED
        );

        if (allDone && journey.getState() != JourneyEnums.JourneyState.COMPLETED) {
            journey.setState(JourneyEnums.JourneyState.COMPLETED);
            journey.setEndDate(LocalDateTime.now());
            journeyRepository.save(journey);
        }
    }

}
