package com.tfg.aegis.emergencycontact;

import com.tfg.aegis.emergencycontact.model.EmergencyContact;
import org.springframework.data.repository.CrudRepository;
import java.util.Set;

public interface EmergencyContactRepository extends CrudRepository<EmergencyContact, Long> {

    Set<EmergencyContact> findByOwnerId(Long ownerId);
}
