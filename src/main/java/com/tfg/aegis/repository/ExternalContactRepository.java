package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.ExternalContact;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.Set;

public interface ExternalContactRepository extends CrudRepository<ExternalContact, Long>  {
    Optional<ExternalContact> findFirstByOwnerIdAndPhone(Long ownerId, String phone);
    Set<ExternalContact> findByOwnerId(Long ownerId);
    Optional<ExternalContact> findByOwnerIdAndPhone(Long ownerId, String phone);
    void deleteByOwnerIdAndPhone(Long ownerId, String phone);
}
