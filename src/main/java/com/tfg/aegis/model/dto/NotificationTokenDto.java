package com.tfg.aegis.model.dto;

import com.tfg.aegis.model.enums.NotificationEnums.Platform;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationTokenDto {
    private Long id;
    private Long userId;
    private String token;
    private Platform platform;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
}
