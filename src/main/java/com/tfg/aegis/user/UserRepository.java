package com.tfg.aegis.user;

import com.tfg.aegis.user.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"emergencyContacts", "safeLocations"})
    Optional<User> findByClerkId(String clerkId);

    boolean existsByPhone(String phone);
}
