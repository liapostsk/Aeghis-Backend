package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.NotificationTokenDto;
import com.tfg.aegis.service.NotificationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification-tokens")
@RequiredArgsConstructor
public class NotificationTokenController {

    private final NotificationTokenService service;
    private static final Logger log = LoggerFactory.getLogger(NotificationTokenController.class);

    @Operation(summary = "Register Notification Token", description = "Registers a notification token for the given user.")
    @PostMapping(path = "/{id}")
    public ResponseEntity<Void> registerToken(@PathVariable(name = "id") Long userId, @RequestBody NotificationTokenDto dto) {
        service.registerToken(userId, dto.getToken(), dto.getPlatform());
        log.info("Registered push token for user {}.", userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Revoke Notification Token", description = "Revokes a notification token for the given user.")
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Void> revokeToken(@PathVariable(name = "id") Long userId, @PathVariable String token) {
        service.revokeToken(userId, token);
        log.info("Revoked push token for user {}.", userId);
        return ResponseEntity.noContent().build();
    }
}
