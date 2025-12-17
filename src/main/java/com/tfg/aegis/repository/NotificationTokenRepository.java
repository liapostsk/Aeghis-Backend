package com.tfg.aegis.repository;

import com.tfg.aegis.model.entity.NotificationToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTokenRepository extends CrudRepository<NotificationToken, Long> {
    List<NotificationToken> findByUser_Id(Long userId);
    Optional<NotificationToken> findByUser_IdAndToken(Long userId, String token);
    void deleteByUser_IdAndToken(Long userId, String token);
}
