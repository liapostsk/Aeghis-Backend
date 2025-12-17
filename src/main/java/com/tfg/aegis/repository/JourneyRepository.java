package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.enums.JourneyEnums;
import com.tfg.aegis.model.entity.Journey;
import org.springframework.data.repository.CrudRepository;

public interface JourneyRepository extends CrudRepository<Journey, Long> {
    Journey findCurrentJourneyByGroupId(Long groupId);

    Journey findByGroupAndState(Group group, JourneyEnums.JourneyState journeyState);
}
