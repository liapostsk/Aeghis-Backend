package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.enums.JourneyEnums;
import com.tfg.aegis.model.entity.Journey;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JourneyRepository extends CrudRepository<Journey, Long> {
    Journey findByGroupAndState(Group group, JourneyEnums.JourneyState journeyState);
    List<Journey> findByState(JourneyEnums.JourneyState state);
}
