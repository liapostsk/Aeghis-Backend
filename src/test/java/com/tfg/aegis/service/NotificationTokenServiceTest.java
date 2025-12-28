package com.tfg.aegis.service;

import com.tfg.aegis.model.entity.NotificationToken;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.enums.NotificationEnums;
import com.tfg.aegis.repository.NotificationTokenRepository;
import com.tfg.aegis.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTokenServiceTest {

    @Mock
    private NotificationTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationTokenService service;

    @Test
    void registerToken_newToken_createsWithProvidedPlatform() {
        Long userId = 1L;
        String token = "expo-token-abc123";
        NotificationEnums.Platform platform = NotificationEnums.Platform.IOS;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndToken(userId, token)).thenReturn(Optional.empty());

        ArgumentCaptor<NotificationToken> captor = ArgumentCaptor.forClass(NotificationToken.class);
        when(tokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registerToken(userId, token, platform);

        NotificationToken saved = captor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals(token, saved.getToken());
        assertEquals(NotificationEnums.Platform.IOS, saved.getPlatform());
        assertNotNull(saved.getCreatedAt());
        verify(tokenRepository).save(any(NotificationToken.class));
    }

    @Test
    void registerToken_newToken_withNullPlatform_defaultsToAndroid() {
        Long userId = 1L;
        String token = "expo-token-xyz789";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndToken(userId, token)).thenReturn(Optional.empty());

        ArgumentCaptor<NotificationToken> captor = ArgumentCaptor.forClass(NotificationToken.class);
        when(tokenRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registerToken(userId, token, null);

        NotificationToken saved = captor.getValue();
        assertEquals(NotificationEnums.Platform.ANDROID, saved.getPlatform()); // Fallback a ANDROID
        verify(tokenRepository).save(any(NotificationToken.class));
    }

    @Test
    void registerToken_existingToken_updatesPlatform() {
        Long userId = 1L;
        String token = "existing-token";
        NotificationEnums.Platform newPlatform = NotificationEnums.Platform.IOS;

        User user = new User();
        user.setId(userId);

        NotificationToken existing = new NotificationToken();
        existing.setId(10L);
        existing.setUser(user);
        existing.setToken(token);
        existing.setPlatform(NotificationEnums.Platform.ANDROID);
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndToken(userId, token)).thenReturn(Optional.of(existing));

        service.registerToken(userId, token, newPlatform);

        assertEquals(NotificationEnums.Platform.IOS, existing.getPlatform());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void registerToken_existingToken_withNullPlatform_doesNotUpdate() {
        Long userId = 1L;
        String token = "existing-token";

        User user = new User();
        user.setId(userId);

        NotificationToken existing = new NotificationToken();
        existing.setId(10L);
        existing.setUser(user);
        existing.setToken(token);
        existing.setPlatform(NotificationEnums.Platform.IOS);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndToken(userId, token)).thenReturn(Optional.of(existing));

        service.registerToken(userId, token, null);

        // Platform no debería cambiar
        assertEquals(NotificationEnums.Platform.IOS, existing.getPlatform());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void registerToken_nullToken_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerToken(1L, null, NotificationEnums.Platform.ANDROID));
    }

    @Test
    void registerToken_blankToken_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerToken(1L, "   ", NotificationEnums.Platform.ANDROID));
    }

    @Test
    void registerToken_emptyToken_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerToken(1L, "", NotificationEnums.Platform.ANDROID));
    }

    @Test
    void registerToken_userNotFound_throwsIllegalArgument() {
        Long userId = 999L;
        String token = "valid-token";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerToken(userId, token, NotificationEnums.Platform.ANDROID));

        assertTrue(ex.getMessage().contains("User not found"));
        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void revokeToken_validToken_deletesFromRepository() {
        Long userId = 1L;
        String token = "token-to-revoke";

        service.revokeToken(userId, token);

        verify(tokenRepository).deleteByUser_IdAndToken(userId, token);
    }

    @Test
    void revokeToken_nullToken_doesNothing() {
        Long userId = 1L;

        service.revokeToken(userId, null);

        verify(tokenRepository, never()).deleteByUser_IdAndToken(anyLong(), anyString());
    }

    @Test
    void revokeToken_blankToken_doesNothing() {
        Long userId = 1L;

        service.revokeToken(userId, "   ");

        verify(tokenRepository, never()).deleteByUser_IdAndToken(anyLong(), anyString());
    }

    @Test
    void revokeToken_emptyToken_doesNothing() {
        Long userId = 1L;

        service.revokeToken(userId, "");

        verify(tokenRepository, never()).deleteByUser_IdAndToken(anyLong(), anyString());
    }

    @Test
    void listByUser_withTokens_returnsList() {
        Long userId = 1L;

        NotificationToken token1 = new NotificationToken();
        token1.setId(1L);
        token1.setToken("token1");

        NotificationToken token2 = new NotificationToken();
        token2.setId(2L);
        token2.setToken("token2");

        when(tokenRepository.findByUser_Id(userId)).thenReturn(List.of(token1, token2));

        List<NotificationToken> result = service.listByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("token1", result.get(0).getToken());
        assertEquals("token2", result.get(1).getToken());
        verify(tokenRepository).findByUser_Id(userId);
    }

    @Test
    void listByUser_noTokens_returnsEmptyList() {
        Long userId = 1L;

        when(tokenRepository.findByUser_Id(userId)).thenReturn(List.of());

        List<NotificationToken> result = service.listByUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tokenRepository).findByUser_Id(userId);
    }

    @Test
    void listByUser_nullUserId_throwsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.listByUser(null));

        assertTrue(ex.getMessage().contains("userId is required"));
        verify(tokenRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void registerToken_multipleCallsSameToken_keepsOnlyOneRecord() {
        Long userId = 1L;
        String token = "same-token";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser_IdAndToken(userId, token))
                .thenReturn(Optional.empty());

        service.registerToken(userId, token, NotificationEnums.Platform.ANDROID);

        verify(tokenRepository, times(1)).save(any(NotificationToken.class));

        NotificationToken existing = new NotificationToken();
        existing.setId(10L);
        existing.setPlatform(NotificationEnums.Platform.ANDROID);

        when(tokenRepository.findByUser_IdAndToken(userId, token))
                .thenReturn(Optional.of(existing));

        service.registerToken(userId, token, NotificationEnums.Platform.IOS);

        assertEquals(NotificationEnums.Platform.IOS, existing.getPlatform());
        verify(tokenRepository, times(1)).save(any(NotificationToken.class)); // Solo 1 vez total
    }

    @Test
    void registerToken_allPlatformTypes_worksCorrectly() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(tokenRepository.findByUser_IdAndToken(eq(userId), eq("token-android")))
                .thenReturn(Optional.empty());
        service.registerToken(userId, "token-android", NotificationEnums.Platform.ANDROID);

        when(tokenRepository.findByUser_IdAndToken(eq(userId), eq("token-ios")))
                .thenReturn(Optional.empty());
        service.registerToken(userId, "token-ios", NotificationEnums.Platform.IOS);

        verify(tokenRepository, times(2)).save(any(NotificationToken.class));
    }
}
