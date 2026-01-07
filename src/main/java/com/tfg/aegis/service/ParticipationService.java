package com.tfg.aegis.service;

import com.tfg.aegis.model.entity.*;
import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.enums.JourneyEnums;
import com.tfg.aegis.repository.*;
import com.tfg.aegis.model.mapper.ParticipationMapper;
import com.tfg.aegis.model.enums.ParticipationEnums;
import com.tfg.aegis.model.dto.ParticipationDto;
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
    private final GroupRepository groupRepository;

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

        if (savedParticipation.getState() == ParticipationEnums.ParticipationState.ARRIVED) {
            markArrived(journey.getId(), user.getId());
        }

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
        Participation p = participationRepository
                .findByJourney_IdAndParticipant_Id(journeyId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Participation not found for journey " + journeyId + " and user " + userId
                ));

        LocalDateTime now = LocalDateTime.now();

        boolean wasArrived = (p.getState() == ParticipationEnums.ParticipationState.ARRIVED);
        if (!wasArrived) {
            p.setState(ParticipationEnums.ParticipationState.ARRIVED);
            if (p.getArrivalTime() == null) p.setArrivalTime(now);
            participationRepository.save(p);
        }

        Journey journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new RuntimeException("Journey not found: " + journeyId));

        boolean allDone = journey.getParticipation().stream().allMatch(part ->
                part.getState() == ParticipationEnums.ParticipationState.ARRIVED
                        || part.getState() == ParticipationEnums.ParticipationState.CANCELLED
        );

        if (allDone && journey.getState() != JourneyEnums.JourneyState.COMPLETED) {
            journey.setState(JourneyEnums.JourneyState.COMPLETED);
            journey.setEndDate(LocalDateTime.now());
            journeyRepository.save(journey);

            Group group = journey.getGroup();
            if (group.getType() == GroupEnums.TypeGroup.TEMPORAL) {
                group.setState(GroupEnums.GroupState.CERRADO);
                group.setLastModified(LocalDateTime.now());
                group.setExpirationDate(LocalDateTime.now().plusHours(24));
                groupRepository.save(group);
            }
        }
    }

    public void deleteParticipation(Long id) {
        Participation participation = participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        participationRepository.delete(participation);
    }
}
