package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.enums.EmergencyContactEnum;
import org.springframework.data.repository.CrudRepository;
import java.util.Set;

public interface EmergencyContactRepository extends CrudRepository<EmergencyContact, Long> {

    Set<EmergencyContact> findByOwnerId(Long ownerId);
    Set<EmergencyContact> findByOwner_IdAndStatus(Long ownerId, EmergencyContactEnum.Status status);

}
