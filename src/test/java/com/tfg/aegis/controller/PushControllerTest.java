package com.tfg.aegis.controller;

import com.tfg.aegis.model.entity.NotificationToken;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.entity.expo.ExpoTicket;
import com.tfg.aegis.model.entity.expo.ExpoReceipt;
import com.tfg.aegis.model.enums.NotificationEnums;
import com.tfg.aegis.service.ExpoPushService;
import com.tfg.aegis.service.NotificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushControllerTest {

    @Mock
    private ExpoPushService expoPushService;

    @Mock
    private NotificationTokenService tokenService;

    @InjectMocks
    private PushController pushController;

    private static final Long USER_ID = 123L;
    private static final String EXPO_TOKEN_1 = "ExponentPushToken[token1]";
    private static final String EXPO_TOKEN_2 = "ExponentPushToken[token2]";
    private static final String TITLE = "Test Title";
    private static final String BODY = "Test Body";
    private static final String CHANNEL_ID = "testChannel";

    private NotificationToken notificationToken1;
    private NotificationToken notificationToken2;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(USER_ID);

        notificationToken1 = new NotificationToken();
        notificationToken1.setId(1L);
        notificationToken1.setToken(EXPO_TOKEN_1);
        notificationToken1.setUser(mockUser);
        notificationToken1.setPlatform(NotificationEnums.Platform.ANDROID);

        notificationToken2 = new NotificationToken();
        notificationToken2.setId(2L);
        notificationToken2.setToken(EXPO_TOKEN_2);
        notificationToken2.setUser(mockUser);
        notificationToken2.setPlatform(NotificationEnums.Platform.IOS);
    }
    
    @Test
    void sendToUser_success() {
        List<NotificationToken> tokens = Arrays.asList(notificationToken1, notificationToken2);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        ExpoTicket ticket1 = new ExpoTicket();
        ticket1.setStatus("ok");
        ticket1.setId("ticket1");
        ticket1.setMessage(null);
        ticket1.setDetails(null);

        ExpoTicket ticket2 = new ExpoTicket();
        ticket2.setStatus("ok");
        ticket2.setId("ticket2");
        ticket2.setMessage(null);
        ticket2.setDetails(null);

        List<ExpoTicket> tickets = Arrays.asList(ticket1, ticket2);
        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID)))
                .thenReturn(tickets);

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, null, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().tokens().size());
        assertEquals(2, response.getBody().tickets().size());
        assertTrue(response.getBody().tokens().contains(EXPO_TOKEN_1));
        assertTrue(response.getBody().tokens().contains(EXPO_TOKEN_2));
        verify(tokenService).listByUser(USER_ID);
        verify(expoPushService).send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID));
    }

    @Test
    void sendToUser_withCustomData_success() {
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        Map<String, Object> customData = new HashMap<>();
        customData.put("key1", "value1");
        customData.put("key2", 123);

        ExpoTicket ticket = new ExpoTicket();
        ticket.setStatus("ok");
        ticket.setId("ticket-id");

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), eq(customData), eq(CHANNEL_ID)))
                .thenReturn(Collections.singletonList(ticket));

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, customData, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().tokens().size());
        verify(expoPushService).send(anyList(), eq(TITLE), eq(BODY), eq(customData), eq(CHANNEL_ID));
    }

    @Test
    void sendToUser_withNullData_usesEmptyMap() {
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        ExpoTicket ticket = new ExpoTicket();
        ticket.setStatus("ok");

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID)))
                .thenReturn(Collections.singletonList(ticket));

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, null, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(expoPushService).send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID));
    }

    @Test
    void sendToUser_withNullChannelId_usesDefault() {
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        ExpoTicket ticket = new ExpoTicket();
        ticket.setStatus("ok");

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq("myNotificationChannel")))
                .thenReturn(Collections.singletonList(ticket));

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, null, null);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(expoPushService).send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq("myNotificationChannel"));
    }

    @Test
    void sendToUser_noTokens_success() {
        when(tokenService.listByUser(USER_ID)).thenReturn(Collections.emptyList());
        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID)))
                .thenReturn(Collections.emptyList());

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, null, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().tokens().isEmpty());
        assertTrue(response.getBody().tickets().isEmpty());
        verify(tokenService).listByUser(USER_ID);
    }

    @Test
    void sendToUser_withDifferentUserId_success() {
        Long differentUserId = 999L;
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(differentUserId)).thenReturn(tokens);

        ExpoTicket ticket = new ExpoTicket();
        ticket.setStatus("ok");

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID)))
                .thenReturn(Collections.singletonList(ticket));

        PushController.SendRequest request = new PushController.SendRequest(
                differentUserId, TITLE, BODY, null, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenService).listByUser(differentUserId);
    }

    @Test
    void sendToUser_withErrorTicket_success() {
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        ExpoTicket errorTicket = new ExpoTicket();
        errorTicket.setStatus("error");
        errorTicket.setId(null);
        errorTicket.setMessage("DeviceNotRegistered");
        errorTicket.setDetails(Map.of("error", "DeviceNotRegistered"));

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID)))
                .thenReturn(Collections.singletonList(errorTicket));

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, null, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().tickets().size());
        assertEquals("error", response.getBody().tickets().get(0).get("status"));
        assertEquals("DeviceNotRegistered", response.getBody().tickets().get(0).get("message"));
    }

    @Test
    void sendToUser_withComplexData_success() {
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        Map<String, Object> complexData = new HashMap<>();
        complexData.put("userId", 123);
        complexData.put("type", "alert");
        complexData.put("metadata", Map.of("key1", "value1", "key2", "value2"));

        ExpoTicket ticket = new ExpoTicket();
        ticket.setStatus("ok");

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), eq(complexData), eq(CHANNEL_ID)))
                .thenReturn(Collections.singletonList(ticket));

        PushController.SendRequest request = new PushController.SendRequest(
                USER_ID, TITLE, BODY, complexData, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> response = pushController.sendToUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(expoPushService).send(anyList(), eq(TITLE), eq(BODY), eq(complexData), eq(CHANNEL_ID));
    }

    @Test
    void receipts_success() {
        List<String> ticketIds = Arrays.asList("ticket1", "ticket2", "ticket3");
        Map<String, ExpoReceipt> mockReceipts = new HashMap<>();
        
        ExpoReceipt receipt1 = new ExpoReceipt();
        receipt1.setStatus("ok");
        mockReceipts.put("ticket1", receipt1);
        
        ExpoReceipt receipt2 = new ExpoReceipt();
        receipt2.setStatus("ok");
        mockReceipts.put("ticket2", receipt2);
        
        ExpoReceipt receipt3 = new ExpoReceipt();
        receipt3.setStatus("error");
        receipt3.setMessage("DeviceNotRegistered");
        mockReceipts.put("ticket3", receipt3);

        when(expoPushService.getReceipts(ticketIds)).thenReturn(mockReceipts);

        PushController.ReceiptsRequest request = new PushController.ReceiptsRequest(ticketIds);

        ResponseEntity<Map<String, Object>> response = pushController.receipts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("receipts"));
        verify(expoPushService).getReceipts(ticketIds);
    }

    @Test
    void receipts_withSingleId_success() {
        List<String> ticketIds = Collections.singletonList("single-ticket-id");
        Map<String, ExpoReceipt> mockReceipts = new HashMap<>();
        
        ExpoReceipt receipt = new ExpoReceipt();
        receipt.setStatus("ok");
        mockReceipts.put("single-ticket-id", receipt);

        when(expoPushService.getReceipts(ticketIds)).thenReturn(mockReceipts);

        PushController.ReceiptsRequest request = new PushController.ReceiptsRequest(ticketIds);

        ResponseEntity<Map<String, Object>> response = pushController.receipts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, ((Map<?, ?>) response.getBody().get("receipts")).size());
        verify(expoPushService).getReceipts(ticketIds);
    }

    @Test
    void receipts_withEmptyList_success() {
        List<String> emptyIds = Collections.emptyList();
        Map<String, ExpoReceipt> emptyReceipts = new HashMap<>();

        when(expoPushService.getReceipts(emptyIds)).thenReturn(emptyReceipts);

        PushController.ReceiptsRequest request = new PushController.ReceiptsRequest(emptyIds);

        ResponseEntity<Map<String, Object>> response = pushController.receipts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((Map<?, ?>) response.getBody().get("receipts")).isEmpty());
        verify(expoPushService).getReceipts(emptyIds);
    }

    @Test
    void receipts_withMultipleIds_success() {
        List<String> ticketIds = Arrays.asList("id1", "id2", "id3", "id4", "id5");
        Map<String, ExpoReceipt> mockReceipts = new HashMap<>();
        for (String id : ticketIds) {
            ExpoReceipt receipt = new ExpoReceipt();
            receipt.setStatus("ok");
            mockReceipts.put(id, receipt);
        }

        when(expoPushService.getReceipts(ticketIds)).thenReturn(mockReceipts);

        PushController.ReceiptsRequest request = new PushController.ReceiptsRequest(ticketIds);

        ResponseEntity<Map<String, Object>> response = pushController.receipts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, ((Map<?, ?>) response.getBody().get("receipts")).size());
        verify(expoPushService).getReceipts(ticketIds);
    }

    @Test
    void receipts_withErrorReceipts_success() {
        List<String> ticketIds = Collections.singletonList("error-ticket");
        Map<String, ExpoReceipt> mockReceipts = new HashMap<>();
        
        ExpoReceipt errorReceipt = new ExpoReceipt();
        errorReceipt.setStatus("error");
        errorReceipt.setMessage("MessageTooBig");
        errorReceipt.setDetails(Map.of("error", "MessageTooBig"));
        mockReceipts.put("error-ticket", errorReceipt);

        when(expoPushService.getReceipts(ticketIds)).thenReturn(mockReceipts);

        PushController.ReceiptsRequest request = new PushController.ReceiptsRequest(ticketIds);

        ResponseEntity<Map<String, Object>> response = pushController.receipts(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> receipts = (Map<?, ?>) response.getBody().get("receipts");
        assertTrue(receipts.containsKey("error-ticket"));
        verify(expoPushService).getReceipts(ticketIds);
    }

    @Test
    void sendAndCheckReceipts_workflow_success() {
        List<NotificationToken> tokens = Collections.singletonList(notificationToken1);
        when(tokenService.listByUser(USER_ID)).thenReturn(tokens);

        ExpoTicket ticket = new ExpoTicket();
        ticket.setStatus("ok");
        ticket.setId("workflow-ticket-id");

        when(expoPushService.send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID)))
                .thenReturn(Collections.singletonList(ticket));

        PushController.SendRequest sendRequest = new PushController.SendRequest(
                USER_ID, TITLE, BODY, null, CHANNEL_ID);

        ResponseEntity<PushController.SendResponse> sendResponse = pushController.sendToUser(sendRequest);

        assertEquals(HttpStatus.OK, sendResponse.getStatusCode());
        assertNotNull(sendResponse.getBody());
        String ticketId = (String) sendResponse.getBody().tickets().get(0).get("id");
        assertEquals("workflow-ticket-id", ticketId);

        Map<String, ExpoReceipt> mockReceipts = new HashMap<>();
        ExpoReceipt receipt = new ExpoReceipt();
        receipt.setStatus("ok");
        mockReceipts.put(ticketId, receipt);
        
        when(expoPushService.getReceipts(Collections.singletonList(ticketId))).thenReturn(mockReceipts);

        PushController.ReceiptsRequest receiptsRequest = new PushController.ReceiptsRequest(
                Collections.singletonList(ticketId));

        ResponseEntity<Map<String, Object>> receiptsResponse = pushController.receipts(receiptsRequest);

        assertEquals(HttpStatus.OK, receiptsResponse.getStatusCode());
        assertNotNull(receiptsResponse.getBody());
        assertTrue(receiptsResponse.getBody().containsKey("receipts"));

        verify(tokenService).listByUser(USER_ID);
        verify(expoPushService).send(anyList(), eq(TITLE), eq(BODY), anyMap(), eq(CHANNEL_ID));
        verify(expoPushService).getReceipts(Collections.singletonList(ticketId));
    }
}
