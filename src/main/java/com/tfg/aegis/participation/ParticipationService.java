package com.tfg.aegis.participation;

import com.tfg.aegis.journey.JourneyRepository;
import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.location.LocationRepository;
import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.participation.mapper.ParticipationMapper;
import com.tfg.aegis.participation.model.Enums;
import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.participation.model.ParticipationDto;
import com.tfg.aegis.person.user.UserRepository;
import com.tfg.aegis.person.user.model.User;
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

        if (participation.getState() == null) {
            participation.setState(Enums.ParticipationState.ACCEPTED);
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
        participationRepository.findById(id).orElseThrow(() -> new RuntimeException("Participation not found"));

        Participation updatedParticipation = participationMapper.toEntity(participationDto);

        // Update fields as necessary
        if (participationDto.getState().equals(Enums.ParticipationState.ARRIVED)) {
            updatedParticipation.setArrivalTime(LocalDateTime.now());
        }
        participationRepository.save(updatedParticipation);
    }

}
