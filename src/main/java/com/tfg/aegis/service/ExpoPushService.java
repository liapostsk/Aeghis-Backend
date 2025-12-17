package com.tfg.aegis.service;

import com.tfg.aegis.model.entity.expo.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpoPushService {

    private static final String EXPO_SEND_URL = "https://exp.host/--/api/v2/push/send";
    private static final String EXPO_RECEIPTS_URL = "https://exp.host/--/api/v2/push/getReceipts";
    private static final int MAX_BATCH = 100;

    private final WebClient web = WebClient.builder().build();

    public List<ExpoTicket> send(List<String> expoTokens,
            String title,
            String body,
            Map<String,Object> data,
            String channelId) {
        List<ExpoMessage> messages = (expoTokens == null ? List.<String>of() : expoTokens).stream()
                .filter(this::isExpoToken)
                .map(t -> {
                    ExpoMessage m = new ExpoMessage();
                    m.setTo(t); m.setTitle(title); m.setBody(body); m.setData(data);
                    m.setChannelId(channelId);
                    return m;
                }).collect(Collectors.toList());

        List<ExpoTicket> tickets = new ArrayList<>();
        for (int i = 0; i < messages.size(); i += MAX_BATCH) {
            List<ExpoMessage> batch = messages.subList(i, Math.min(i + MAX_BATCH, messages.size()));
            ExpoSendResponse resp = web.post().uri(EXPO_SEND_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(batch) // array de mensajes
                    .retrieve()
                    .bodyToMono(ExpoSendResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (resp != null && resp.getData() != null) tickets.addAll(resp.getData());
        }
        return tickets;
    }

    public Map<String, ExpoReceipt> getReceipts(Collection<String> ticketIds) {
        if (ticketIds == null || ticketIds.isEmpty()) return Map.of();
        ExpoReceiptsResponse resp = web.post().uri(EXPO_RECEIPTS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("ids", ticketIds))
                .retrieve()
                .bodyToMono(ExpoReceiptsResponse.class)
                .timeout(Duration.ofSeconds(15))
                .block();
        return (resp != null && resp.getData() != null) ? resp.getData() : Map.of();
    }

    private boolean isExpoToken(String t) {
        return t != null && t.startsWith("ExponentPushToken[");
    }
}
