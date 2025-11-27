package com.tfg.aegis.journey;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.common.utils.Utils;
import com.tfg.aegis.group.GroupRepository;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.journey.mapper.JourneyMapper;
import com.tfg.aegis.journey.model.Enums;
import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.participation.ParticipationRepository;
import com.tfg.aegis.participation.mapper.ParticipationMapper;
import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.person.user.UserService;
import com.tfg.aegis.person.user.model.UserDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class JourneyService {
    private final JourneyRepository journeyRepository;
    private final JourneyMapper journeyMapper;
    private final ParticipationMapper participationMapper;
    private final ParticipationRepository participationRepository;
    private final GroupRepository groupRepository;
    private final UserService userService;

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
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group with id %s not found".formatted(groupId)));

        Journey journey = journeyRepository.findByGroupAndState(group, Enums.JourneyState.IN_PROGRESS);
        if (journey == null) {
            journey = journeyRepository.findByGroupAndState(group, Enums.JourneyState.PENDING);
        }
        if (journey == null) {
            throw new RuntimeException("No current journey found for group with id %s".formatted(groupId));
        }
        return journeyMapper.toDto(journey);
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

        journey.setParticipations(new HashSet<>());

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

    /**
     * Adds a participation to a journey.
     *
     * @param journeyId The ID of the journey.
     * @param participationId The ID of the participation to be added.
     */
    public void addParticipationToJourney(Long journeyId, Long participationId) {
        Journey journey = journeyRepository.findById(journeyId)
            .orElseThrow(() -> new RuntimeException("Journey with id %s not found".formatted(journeyId)));
        Participation participation = participationRepository.findById(participationId)
            .orElseThrow(() -> new RuntimeException("Participation with id %s not found".formatted(participationId)));
        journey.getParticipations().add(participation);
        journeyRepository.save(journey);
    }

    // Cuando en un trayecto todas sus participaciones estén a "FINALIZADA", el trayecto pasará a estar en estado "FINALIZADO".

    /**
     * Checks if a user is a participant in a journey.
     *
     * @param journeyId The ID of the journey.
     * @return True if the user is a participant in the journey, false otherwise.
     */
    public boolean isUserParticipantInJourney(Long journeyId) {
        journeyRepository.findById(journeyId).orElseThrow(() -> new RuntimeException("Journey with id %s not found".formatted(journeyId)));
        return participationRepository.existsByJourney_IdAndParticipant_Id(journeyId, getCurrentUser().getId());
    }

    /**
     * Retrieves all participants of a journey.
     *
     * @param journeyId The ID of the journey.
     * @return A set of participant IDs in the journey.
     */
    public Set<Long> getAllParticipantsOfJourney(Long journeyId) {
        Journey journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new NotFoundException("Journey not found with id: " + journeyId));
        if (journey.getParticipations() == null) {
            return Set.of();
        }
        return journey.getParticipations().stream()
                .map(p -> p.getParticipant().getId())
                .collect(Collectors.toSet());
    }

    private UserDto getCurrentUser() {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByClerkId(clerkId);
    }

    /**
     * Changes the status of a journey.
     *
     * @param journeyId The ID of the journey.
     * @param status The new status to be set.
     */
    public void changeJourneyStatus(Long journeyId, String status) {
        Journey journey = journeyRepository.findById(journeyId).orElseThrow(() -> new NotFoundException("Journey not found with id: " + journeyId));
        Enums.JourneyState newState;
        try {
            newState = Enums.JourneyState.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid journey state: %s".formatted(status));
        }
        journey.setState(newState);
        journeyRepository.save(journey);
    }

}
