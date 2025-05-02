package com.tfg.aegis.user;

import com.tfg.aegis.user.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String email);
}
