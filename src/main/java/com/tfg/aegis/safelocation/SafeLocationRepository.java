package com.tfg.aegis.safelocation;

import com.tfg.aegis.safelocation.model.SafeLocation;
import com.tfg.aegis.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;

public interface SafeLocationRepository extends JpaRepository<SafeLocation, Long> {
    Set<SafeLocation> findByOwner(User owner);
}
