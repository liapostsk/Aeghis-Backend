package com.tfg.aegis.repository;


import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.enums.CompanionRequestEnums;
import com.tfg.aegis.model.entity.Location;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompanionRequestRepository extends CrudRepository<CompanionRequest, Long> {

    List<CompanionRequest> findByDestinationAndAproxHourBetween(Location destination, LocalDateTime from, LocalDateTime to);

    List<CompanionRequest> findByCreatorIdOrCompanionId(Long userId, Long companionId);

    List<CompanionRequest> findByAproxHourBetween(LocalDateTime from, LocalDateTime to);

    boolean existsByCreatorIdAndState(Long userId, CompanionRequestEnums.RequestStatus requestStatus);

    Optional<CompanionRequest> findByCompanionGroupId(Long groupId);

    List<CompanionRequest> findByCompanion_Id(Long companionId);
}
