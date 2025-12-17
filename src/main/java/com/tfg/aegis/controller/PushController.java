package com.tfg.aegis.controller;

import com.tfg.aegis.service.ExpoPushService;
import com.tfg.aegis.service.NotificationTokenService;
import com.tfg.aegis.model.entity.NotificationToken;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
public class PushController {

    private final ExpoPushService expoPushService;
    private final NotificationTokenService tokenService;

    public record SendRequest(Long userId, String title, String body, Map<String,Object> data, String channelId) {}
    public record SendResponse(List<String> tokens, List<Map<String,Object>> tickets) {}

    private static final Logger log = LoggerFactory.getLogger(PushController.class);

    @Operation(summary = "Enviar push a un usuario (via Expo)")
    @PostMapping("/sendToUser")
    public ResponseEntity<SendResponse> sendToUser(@RequestBody SendRequest req) {
        List<NotificationToken> tokens = tokenService.listByUser(req.userId());
        List<String> expoTokens = tokens.stream().map(NotificationToken::getToken).toList();

        log.info("Sending push to user {}: title {}, body {}, tokens {}",
                req.userId(), req.title(), req.body(), expoTokens);

        var tickets = expoPushService.send(
                expoTokens, req.title(), req.body(),
                Optional.ofNullable(req.data()).orElseGet(HashMap::new),
                Optional.ofNullable(req.channelId()).orElse("myNotificationChannel")
        );

        // Devuelvo tokens y tickets (id/status).
        List<Map<String,Object>> t = tickets.stream().map(ticket -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("status", ticket.getStatus());
            m.put("id", ticket.getId());
            m.put("message", ticket.getMessage());
            m.put("details", ticket.getDetails());
            return m;
        }).toList();

        return ResponseEntity.ok(new SendResponse(expoTokens, t));
    }

    public record ReceiptsRequest(List<String> ids) {}

    @Operation(summary = "Consultar receipts de tickets (de Expo)")
    @PostMapping("/receipts")
    public ResponseEntity<Map<String,Object>> receipts(@RequestBody ReceiptsRequest req) {
        var receipts = expoPushService.getReceipts(req.ids());
        var body = new LinkedHashMap<String,Object>();
        body.put("receipts", receipts);
        return ResponseEntity.ok(body);
    }
}