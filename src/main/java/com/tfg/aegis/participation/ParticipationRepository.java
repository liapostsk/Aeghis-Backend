package com.tfg.aegis.participation;

import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.participation.model.Participation;
import com.tfg.aegis.person.user.model.User;
import org.springframework.data.repository.CrudRepository;

public interface ParticipationRepository extends CrudRepository<Participation, Long> {
    boolean existsByJourneyAndParticipant(Journey journey, User user);

    boolean existsByJourney_IdAndParticipant_Id(Long journeyId, Long userId);
}
