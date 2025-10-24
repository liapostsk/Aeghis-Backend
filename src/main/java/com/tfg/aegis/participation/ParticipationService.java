package com.tfg.aegis.participation;

import com.tfg.aegis.participation.mapper.ParticipationMapper;
import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.participation.model.ParticipationDto;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final ParticipationMapper participationMapper;

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
        Participation participation = participationMapper.toEntity(participationDto);
        Participation savedParticipation = participationRepository.save(participation);
        return savedParticipation.getId();
    }

    /**
     * Update an existing participation.
     * @param participationDto the ParticipationDto object
     */
    public void updateParticipation(ParticipationDto participationDto) {
        Long id = participationDto.getId();
        Participation existingParticipation = participationRepository.findById(id).orElseThrow(() -> new RuntimeException("Participation not found"));
        // Update fields as necessary
        participationRepository.save(existingParticipation);
    }

}
