package com.tfg.aegis.journey;

import com.tfg.aegis.journey.model.Journey;
import com.tfg.aegis.journey.model.JourneyDto;
import org.springframework.data.repository.CrudRepository;

public interface JourneyRepository extends CrudRepository<Journey, Long> {
    Journey findCurrentJourneyByGroupId(Long groupId);
}
