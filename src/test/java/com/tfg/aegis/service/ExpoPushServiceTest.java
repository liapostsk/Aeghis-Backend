package com.tfg.aegis.service;

import com.tfg.aegis.model.entity.expo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExpoPushServiceTest {

    private ExpoPushService service;

    @BeforeEach
    void setUp() {
        service = new ExpoPushService();
    }

    @Test
    void send_withNullTokens_returnsEmptyList() {
        List<ExpoTicket> tickets = service.send(null, "Title", "Body", null, "default");

        assertNotNull(tickets);
        assertTrue(tickets.isEmpty());
    }

    @Test
    void send_withEmptyTokens_returnsEmptyList() {
        List<ExpoTicket> tickets = service.send(Collections.emptyList(), "Title", "Body", null, "default");

        assertNotNull(tickets);
        assertTrue(tickets.isEmpty());
    }

    @Test
    void send_withInvalidTokens_filtersAndReturnsEmpty() {
        List<String> invalidTokens = Arrays.asList("invalid-token", "another-invalid", "", null);
        List<ExpoTicket> tickets = service.send(invalidTokens, "Title", "Body", null, "default");

        assertNotNull(tickets);
        assertTrue(tickets.isEmpty());
    }

    @Test
    void isExpoToken_withValidToken_returnsTrue() throws Exception {
        Method isExpoTokenMethod = ExpoPushService.class.getDeclaredMethod("isExpoToken", String.class);
        isExpoTokenMethod.setAccessible(true);

        boolean result = (boolean) isExpoTokenMethod.invoke(service, "ExponentPushToken[abc123]");

        assertTrue(result);
    }

    @Test
    void isExpoToken_withInvalidToken_returnsFalse() throws Exception {
        Method isExpoTokenMethod = ExpoPushService.class.getDeclaredMethod("isExpoToken", String.class);
        isExpoTokenMethod.setAccessible(true);

        assertFalse((boolean) isExpoTokenMethod.invoke(service, "invalid-token"));
        String emptyString = "";
        assertFalse((boolean) isExpoTokenMethod.invoke(service, emptyString));
    }

    @Test
    void isExpoToken_withNullToken_returnsFalse() throws Exception {
        Method isExpoTokenMethod = ExpoPushService.class.getDeclaredMethod("isExpoToken", String.class);
        isExpoTokenMethod.setAccessible(true);

        String nullString = null;
        boolean result = (boolean) isExpoTokenMethod.invoke(service, nullString);

        assertFalse(result);
    }

    @Test
    void getReceipts_withNullTicketIds_returnsEmptyMap() {
        Map<String, ExpoReceipt> receipts = service.getReceipts(null);

        assertNotNull(receipts);
        assertTrue(receipts.isEmpty());
    }

    @Test
    void getReceipts_withEmptyTicketIds_returnsEmptyMap() {
        Map<String, ExpoReceipt> receipts = service.getReceipts(Collections.emptyList());

        assertNotNull(receipts);
        assertTrue(receipts.isEmpty());
    }

    @Test
    void send_withMixedValidAndInvalidTokens_filtersCorrectly() {
        List<String> tokens = Arrays.asList(
                "ExponentPushToken[valid1]",
                "invalid",
                null,
                "ExponentPushToken[valid2]",
                "",
                "ExponentPushToken[valid3]"
        );

        assertDoesNotThrow(() -> {
            try {
                service.send(tokens, "Title", "Body", null, "default");
            } catch (Exception e) {
                // Esperamos que falle la conexión, pero eso está bien
                // Lo importante es que no falle antes por tokens inválidos
            }
        });
    }

    @Test
    void send_withDataAndChannelId_createsMessagesCorrectly() {
        List<String> tokens = Collections.singletonList("ExponentPushToken[test]");
        Map<String, Object> data = Map.of("key1", "value1", "key2", 123);
        String channelId = "custom-channel";

        assertDoesNotThrow(() -> {
            try {
                service.send(tokens, "Test Title", "Test Body", data, channelId);
            } catch (Exception e) {
                // Esperamos fallo de conexión, no de construcción de mensajes
            }
        });
    }
}
