package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.EmergencyContact;
import org.springframework.data.repository.CrudRepository;
import java.util.Set;

public interface EmergencyContactRepository extends CrudRepository<EmergencyContact, Long> {

    Set<EmergencyContact> findByOwnerId(Long ownerId);
}
