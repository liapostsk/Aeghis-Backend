package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.Journey;
import com.tfg.aegis.model.entity.Participation;
import com.tfg.aegis.model.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ParticipationRepository extends CrudRepository<Participation, Long> {
    boolean existsByJourneyAndParticipant(Journey journey, User user);

    boolean existsByJourney_IdAndParticipant_Id(Long journeyId, Long userId);

    Optional<Participation> findByJourney_IdAndParticipant_Id(Long journeyId, Long userId);
}
