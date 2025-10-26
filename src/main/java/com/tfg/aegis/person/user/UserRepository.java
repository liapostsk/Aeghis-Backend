package com.tfg.aegis.person.user;

import com.tfg.aegis.person.user.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    @EntityGraph(attributePaths = {"emergencyContacts", "safeLocations"})
    Optional<User> findByClerkId(String clerkId);

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phoneE164);
}
