package com.tfg.aegis.notification;

import com.tfg.aegis.notification.model.NotificationToken;
import com.tfg.aegis.notification.model.enums;
import com.tfg.aegis.person.user.UserRepository;
import com.tfg.aegis.person.user.model.User;
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
     * Registro de token con plataforma explícita.
     * Si ya existe para el usuario, actualiza lastSeenAt y (si viene) la plataforma.
     */
    public void registerToken(Long userId, String token, enums.Platform platform) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }
        // Busca el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Si ya existe el token para el usuario, sólo refrescamos metadatos
        tokenRepository.findByUser_IdAndToken(userId, token).ifPresentOrElse(existing -> {
            existing.setLastSeenAt(LocalDateTime.now());
            if (platform != null) {
                existing.setPlatform(platform);
            }
            // JPA dirty checking persistirá cambios
        }, () -> {
            // Si no existe, lo creamos
            NotificationToken nt = new NotificationToken();
            nt.setUser(user);
            nt.setToken(token);
            nt.setPlatform(platform != null ? platform : enums.Platform.ANDROID); // fallback sensato
            nt.setCreatedAt(LocalDateTime.now());
            nt.setLastSeenAt(LocalDateTime.now());
            tokenRepository.save(nt);
        });
    }

    /**
     * Overload por compatibilidad si aún no envías platform desde el cliente.
     * Ajusta el fallback si lo prefieres.
     */
    public void registerToken(Long userId, String token) {
        registerToken(userId, token, enums.Platform.ANDROID);
    }

    /**
     * Revoca un token concreto del usuario (borra la fila).
     */
    public void revokeToken(Long userId, String token) {
        if (token == null || token.isBlank()) return;
        tokenRepository.deleteByUser_IdAndToken(userId, token);
    }

    public List<NotificationToken> listByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return tokenRepository.findByUser_Id(userId);
    }
}
