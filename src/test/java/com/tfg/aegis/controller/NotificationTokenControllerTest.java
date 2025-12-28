package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.NotificationTokenDto;
import com.tfg.aegis.model.enums.NotificationEnums;
import com.tfg.aegis.service.NotificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTokenControllerTest {

    @Mock
    private NotificationTokenService notificationTokenService;

    @InjectMocks
    private NotificationTokenController notificationTokenController;

    private NotificationTokenDto notificationTokenDto;
    private static final Long USER_ID = 123L;
    private static final String EXPO_TOKEN = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]";
    private static final String FCM_TOKEN = "fcm-token-12345-abcde-67890";
    private static final NotificationEnums.Platform PLATFORM_ANDROID = NotificationEnums.Platform.ANDROID;
    private static final NotificationEnums.Platform PLATFORM_IOS = NotificationEnums.Platform.IOS;

    @BeforeEach
    void setUp() {
        notificationTokenDto = new NotificationTokenDto();
        notificationTokenDto.setToken(EXPO_TOKEN);
        notificationTokenDto.setPlatform(PLATFORM_ANDROID);
    }
    
    @Test
    void registerToken_success() {
        doNothing().when(notificationTokenService).registerToken(USER_ID, EXPO_TOKEN, PLATFORM_ANDROID);

        ResponseEntity<Void> response = notificationTokenController.registerToken(USER_ID, notificationTokenDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(notificationTokenService).registerToken(USER_ID, EXPO_TOKEN, PLATFORM_ANDROID);
    }

    @Test
    void registerToken_withIOSPlatform_success() {
        NotificationTokenDto iosToken = new NotificationTokenDto();
        iosToken.setToken(EXPO_TOKEN);
        iosToken.setPlatform(PLATFORM_IOS);
        doNothing().when(notificationTokenService).registerToken(USER_ID, EXPO_TOKEN, PLATFORM_IOS);

        ResponseEntity<Void> response = notificationTokenController.registerToken(USER_ID, iosToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).registerToken(USER_ID, EXPO_TOKEN, PLATFORM_IOS);
    }

    @Test
    void registerToken_withFCMToken_success() {
        NotificationTokenDto fcmToken = new NotificationTokenDto();
        fcmToken.setToken(FCM_TOKEN);
        fcmToken.setPlatform(PLATFORM_ANDROID);
        doNothing().when(notificationTokenService).registerToken(USER_ID, FCM_TOKEN, PLATFORM_ANDROID);

        ResponseEntity<Void> response = notificationTokenController.registerToken(USER_ID, fcmToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).registerToken(USER_ID, FCM_TOKEN, PLATFORM_ANDROID);
    }

    @Test
    void registerToken_withDifferentUserId_success() {
        Long differentUserId = 999L;
        doNothing().when(notificationTokenService).registerToken(differentUserId, EXPO_TOKEN, PLATFORM_ANDROID);

        ResponseEntity<Void> response = notificationTokenController.registerToken(differentUserId, notificationTokenDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).registerToken(differentUserId, EXPO_TOKEN, PLATFORM_ANDROID);
    }

    @Test
    void registerToken_withLongToken_success() {
        String longToken = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx]";
        NotificationTokenDto longTokenDto = new NotificationTokenDto();
        longTokenDto.setToken(longToken);
        longTokenDto.setPlatform(PLATFORM_ANDROID);
        doNothing().when(notificationTokenService).registerToken(USER_ID, longToken, PLATFORM_ANDROID);

        ResponseEntity<Void> response = notificationTokenController.registerToken(USER_ID, longTokenDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).registerToken(USER_ID, longToken, PLATFORM_ANDROID);
    }


    @Test
    void registerToken_multipleUsers_success() {
        Long userId1 = 1L, userId2 = 2L, userId3 = 3L;
        doNothing().when(notificationTokenService).registerToken(anyLong(), anyString(), any(NotificationEnums.Platform.class));

        ResponseEntity<Void> response1 = notificationTokenController.registerToken(userId1, notificationTokenDto);
        ResponseEntity<Void> response2 = notificationTokenController.registerToken(userId2, notificationTokenDto);
        ResponseEntity<Void> response3 = notificationTokenController.registerToken(userId3, notificationTokenDto);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(notificationTokenService).registerToken(userId1, EXPO_TOKEN, PLATFORM_ANDROID);
        verify(notificationTokenService).registerToken(userId2, EXPO_TOKEN, PLATFORM_ANDROID);
        verify(notificationTokenService).registerToken(userId3, EXPO_TOKEN, PLATFORM_ANDROID);
    }

    @Test
    void registerToken_sameUserDifferentTokens_success() {
        String token1 = "ExponentPushToken[token1]";
        String token2 = "ExponentPushToken[token2]";
        NotificationTokenDto dto1 = new NotificationTokenDto();
        dto1.setToken(token1);
        dto1.setPlatform(PLATFORM_ANDROID);
        NotificationTokenDto dto2 = new NotificationTokenDto();
        dto2.setToken(token2);
        dto2.setPlatform(PLATFORM_IOS);

        doNothing().when(notificationTokenService).registerToken(anyLong(), anyString(), any(NotificationEnums.Platform.class));

        ResponseEntity<Void> response1 = notificationTokenController.registerToken(USER_ID, dto1);
        ResponseEntity<Void> response2 = notificationTokenController.registerToken(USER_ID, dto2);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        verify(notificationTokenService).registerToken(USER_ID, token1, PLATFORM_ANDROID);
        verify(notificationTokenService).registerToken(USER_ID, token2, PLATFORM_IOS);
    }

    @Test
    void revokeToken_success() {
        doNothing().when(notificationTokenService).revokeToken(USER_ID, EXPO_TOKEN);

        ResponseEntity<Void> response = notificationTokenController.revokeToken(USER_ID, EXPO_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(notificationTokenService).revokeToken(USER_ID, EXPO_TOKEN);
    }

    @Test
    void revokeToken_withFCMToken_success() {
        doNothing().when(notificationTokenService).revokeToken(USER_ID, FCM_TOKEN);

        ResponseEntity<Void> response = notificationTokenController.revokeToken(USER_ID, FCM_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).revokeToken(USER_ID, FCM_TOKEN);
    }

    @Test
    void revokeToken_withDifferentUserId_success() {
        Long differentUserId = 777L;
        doNothing().when(notificationTokenService).revokeToken(differentUserId, EXPO_TOKEN);

        ResponseEntity<Void> response = notificationTokenController.revokeToken(differentUserId, EXPO_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).revokeToken(differentUserId, EXPO_TOKEN);
    }

    @Test
    void revokeToken_withLongToken_success() {
        String longToken = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx]";
        doNothing().when(notificationTokenService).revokeToken(USER_ID, longToken);

        ResponseEntity<Void> response = notificationTokenController.revokeToken(USER_ID, longToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).revokeToken(USER_ID, longToken);
    }

    @Test
    void revokeToken_multipleUsers_success() {
        Long userId1 = 10L, userId2 = 20L, userId3 = 30L;
        doNothing().when(notificationTokenService).revokeToken(anyLong(), anyString());

        ResponseEntity<Void> response1 = notificationTokenController.revokeToken(userId1, EXPO_TOKEN);
        ResponseEntity<Void> response2 = notificationTokenController.revokeToken(userId2, EXPO_TOKEN);
        ResponseEntity<Void> response3 = notificationTokenController.revokeToken(userId3, EXPO_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(notificationTokenService).revokeToken(userId1, EXPO_TOKEN);
        verify(notificationTokenService).revokeToken(userId2, EXPO_TOKEN);
        verify(notificationTokenService).revokeToken(userId3, EXPO_TOKEN);
    }

    @Test
    void revokeToken_multipleTokens_success() {
        String token1 = "ExponentPushToken[token1]";
        String token2 = "ExponentPushToken[token2]";
        String token3 = "fcm-token-12345";
        doNothing().when(notificationTokenService).revokeToken(anyLong(), anyString());

        ResponseEntity<Void> response1 = notificationTokenController.revokeToken(USER_ID, token1);
        ResponseEntity<Void> response2 = notificationTokenController.revokeToken(USER_ID, token2);
        ResponseEntity<Void> response3 = notificationTokenController.revokeToken(USER_ID, token3);

        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode());
        verify(notificationTokenService).revokeToken(USER_ID, token1);
        verify(notificationTokenService).revokeToken(USER_ID, token2);
        verify(notificationTokenService).revokeToken(USER_ID, token3);
    }

    @Test
    void revokeToken_withSpecialCharactersInToken_success() {
        String specialToken = "ExponentPushToken[abc-123_xyz.456]";
        doNothing().when(notificationTokenService).revokeToken(USER_ID, specialToken);

        ResponseEntity<Void> response = notificationTokenController.revokeToken(USER_ID, specialToken);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationTokenService).revokeToken(USER_ID, specialToken);
    }

    @Test
    void registerAndRevoke_workflow_success() {
        String token = "ExponentPushToken[workflow-token]";
        NotificationTokenDto tokenDto = new NotificationTokenDto();
        tokenDto.setToken(token);
        tokenDto.setPlatform(PLATFORM_ANDROID);

        doNothing().when(notificationTokenService).registerToken(USER_ID, token, PLATFORM_ANDROID);
        doNothing().when(notificationTokenService).revokeToken(USER_ID, token);

        ResponseEntity<Void> registerResponse = notificationTokenController.registerToken(USER_ID, tokenDto);

        assertEquals(HttpStatus.NO_CONTENT, registerResponse.getStatusCode());
        verify(notificationTokenService).registerToken(USER_ID, token, PLATFORM_ANDROID);

        ResponseEntity<Void> revokeResponse = notificationTokenController.revokeToken(USER_ID, token);

        assertEquals(HttpStatus.NO_CONTENT, revokeResponse.getStatusCode());
        verify(notificationTokenService).revokeToken(USER_ID, token);
    }

    @Test
    void multipleRegistrationsAndRevocations_workflow_success() {
        String token1 = "ExponentPushToken[device1]";
        String token2 = "ExponentPushToken[device2]";
        NotificationTokenDto dto1 = new NotificationTokenDto();
        dto1.setToken(token1);
        dto1.setPlatform(PLATFORM_ANDROID);
        NotificationTokenDto dto2 = new NotificationTokenDto();
        dto2.setToken(token2);
        dto2.setPlatform(PLATFORM_IOS);

        doNothing().when(notificationTokenService).registerToken(anyLong(), anyString(), any(NotificationEnums.Platform.class));
        doNothing().when(notificationTokenService).revokeToken(anyLong(), anyString());

        notificationTokenController.registerToken(USER_ID, dto1);
        notificationTokenController.registerToken(USER_ID, dto2);

        ResponseEntity<Void> revoke1 = notificationTokenController.revokeToken(USER_ID, token1);
        ResponseEntity<Void> revoke2 = notificationTokenController.revokeToken(USER_ID, token2);

        assertEquals(HttpStatus.NO_CONTENT, revoke1.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, revoke2.getStatusCode());
        verify(notificationTokenService).registerToken(USER_ID, token1, PLATFORM_ANDROID);
        verify(notificationTokenService).registerToken(USER_ID, token2, PLATFORM_IOS);
        verify(notificationTokenService).revokeToken(USER_ID, token1);
        verify(notificationTokenService).revokeToken(USER_ID, token2);
    }
}
