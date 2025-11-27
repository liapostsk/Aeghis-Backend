package com.tfg.aegis.journey;

import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.journey.model.Enums;
import com.tfg.aegis.journey.model.Journey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface JourneyRepository extends CrudRepository<Journey, Long> {
    Journey findCurrentJourneyByGroupId(Long groupId);

    Journey findByGroupAndState(Group group, Enums.JourneyState journeyState);
}
