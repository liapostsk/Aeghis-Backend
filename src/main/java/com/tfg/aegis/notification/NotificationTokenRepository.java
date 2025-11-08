package com.tfg.aegis.notification;

import com.tfg.aegis.notification.model.NotificationToken;
import com.tfg.aegis.notification.model.enums.Platform;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTokenRepository extends CrudRepository<NotificationToken, Long> {
    List<NotificationToken> findByUser_Id(Long userId);
    Optional<NotificationToken> findByUser_IdAndToken(Long userId, String token);
    boolean existsByUser_IdAndToken(Long userId, String token);
    void deleteByUser_IdAndToken(Long userId, String token);
    long deleteByToken(String token);
    List<NotificationToken> findByPlatform(Platform platform);
}
