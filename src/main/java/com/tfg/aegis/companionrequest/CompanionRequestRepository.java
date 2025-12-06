package com.tfg.aegis.companionrequest;


import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.location.model.Location;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompanionRequestRepository extends CrudRepository<CompanionRequest, Long> {

    List<CompanionRequest> findByDestinationAndAproxHourBetween(Location destination, LocalDateTime from, LocalDateTime to);

    List<CompanionRequest> findByCreatorIdOrCompanionId(Long userId, Long companionId);
}
