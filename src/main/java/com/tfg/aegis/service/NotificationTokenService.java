package com.tfg.aegis.service;

import com.tfg.aegis.model.entity.NotificationToken;
import com.tfg.aegis.model.enums.NotificationEnums;
import com.tfg.aegis.repository.NotificationTokenRepository;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class NotificationTokenService {

    private final NotificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    /**
     * Registra un token de notificación para un usuario.
     * Si el token ya existe, actualiza la fecha de último uso y plataforma si se proporciona.
     *
     * @param userId   ID del usuario
     * @param token    Token de notificación
     * @param platform Plataforma del token
     */
    public void registerToken(Long userId, String token, NotificationEnums.Platform platform) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        tokenRepository.findByUser_IdAndToken(userId, token).ifPresentOrElse(existing -> {
            if (platform != null) {
                existing.setPlatform(platform);
            }
        }, () -> {
            NotificationToken nt = new NotificationToken();
            nt.setUser(user);
            nt.setToken(token);
            nt.setPlatform(platform != null ? platform : NotificationEnums.Platform.ANDROID); // fallback sensato
            nt.setCreatedAt(LocalDateTime.now());
            tokenRepository.save(nt);
        });
    }

    /**
     * Revoca un token concreto del usuario (borra la fila).
     */
    public void revokeToken(Long userId, String token) {
        if (token == null || token.isBlank()) return;
        tokenRepository.deleteByUser_IdAndToken(userId, token);
    }

    /**
     * Encuentra todos los tokens asociados a un usuario.
     */
    public List<NotificationToken> listByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return tokenRepository.findByUser_Id(userId);
    }
}
