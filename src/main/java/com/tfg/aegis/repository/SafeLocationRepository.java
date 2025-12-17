package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.SafeLocation;
import com.tfg.aegis.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;

public interface SafeLocationRepository extends JpaRepository<SafeLocation, Long> {
    Set<SafeLocation> findByOwner(User owner);
}
