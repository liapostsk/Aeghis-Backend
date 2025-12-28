package com.tfg.aegis.service;

import com.tfg.aegis.model.dto.EmergencyTriggerRequestDto;
import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.entity.NotificationToken;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.enums.EmergencyContactEnum;
import com.tfg.aegis.repository.EmergencyContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyAlertServiceTest {

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private NotificationTokenService tokenService;

    @Mock
    private ExpoPushService expoPushService;

    @Mock
    private GeocodingService geocodingService;

    @InjectMocks
    private EmergencyAlertService service;

    private User owner;
    private User contact1;
    private User contact2;
    private EmergencyContact emergencyContact1;
    private EmergencyContact emergencyContact2;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);

        contact1 = new User();
        contact1.setId(2L);

        contact2 = new User();
        contact2.setId(3L);

        emergencyContact1 = new EmergencyContact();
        emergencyContact1.setId(1L);
        emergencyContact1.setOwner(owner);
        emergencyContact1.setContact(contact1);
        emergencyContact1.setStatus(EmergencyContactEnum.Status.ACCEPTED);

        emergencyContact2 = new EmergencyContact();
        emergencyContact2.setId(2L);
        emergencyContact2.setOwner(owner);
        emergencyContact2.setContact(contact2);
        emergencyContact2.setStatus(EmergencyContactEnum.Status.ACCEPTED);
    }

    @Test
    void trigger_withAcceptedContacts_sendsNotifications() {
        // Given
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("I need help!");

        Set<EmergencyContact> contacts = new HashSet<>(Arrays.asList(emergencyContact1, emergencyContact2));

        NotificationToken token1 = new NotificationToken();
        token1.setToken("ExponentPushToken[token1]");
        NotificationToken token2 = new NotificationToken();
        token2.setToken("ExponentPushToken[token2]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Barcelona, Spain");
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token1));
        when(tokenService.listByUser(3L)).thenReturn(Collections.singletonList(token2));

        // When
        service.trigger(ownerId, ownerName, request);

        // Then
        verify(emergencyContactRepository).findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED);
        verify(geocodingService).reverseGeocodeAddress(41.3874, 2.1686);
        verify(tokenService).listByUser(2L);
        verify(tokenService).listByUser(3L);
        verify(expoPushService, times(2)).send(anyList(), anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void trigger_withNoMessage_usesDefaultMessage() {
        // Given
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage(null);

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token = new NotificationToken();
        token.setToken("ExponentPushToken[token1]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Barcelona, Spain");
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token));

        // When
        service.trigger(ownerId, ownerName, request);

        // Then
        verify(expoPushService).send(anyList(), anyString(), contains(ownerName), anyMap(), anyString());
    }

    @Test
    void trigger_withBlankMessage_usesDefaultMessage() {
        // Given
        Long ownerId = 1L;
        String ownerName = "Jane Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("   ");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token = new NotificationToken();
        token.setToken("ExponentPushToken[token1]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Madrid, Spain");
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token));

        // When
        service.trigger(ownerId, ownerName, request);

        // Then
        verify(expoPushService).send(anyList(), anyString(), contains(ownerName), anyMap(), anyString());
    }

    @Test
    void trigger_withNullAddress_doesNotIncludeAddress() {
        // Given
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("Help!");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token = new NotificationToken();
        token.setToken("ExponentPushToken[token1]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn(null);
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token));

        // When
        service.trigger(ownerId, ownerName, request);

        // Then
        verify(expoPushService).send(anyList(), anyString(), eq("Help!"), anyMap(), anyString());
    }

    @Test
    void trigger_withBlankAddress_doesNotIncludeAddress() {
        // Given
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("Emergency!");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token = new NotificationToken();
        token.setToken("ExponentPushToken[token1]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("   ");
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token));

        // When
        service.trigger(ownerId, ownerName, request);

        // Then
        verify(expoPushService).send(anyList(), anyString(), eq("Emergency!"), anyMap(), anyString());
    }

    @Test
    void trigger_withNoAcceptedContacts_doesNotSendNotifications() {
        // Given
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("Help!");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(Collections.emptySet());

        // When
        service.trigger(ownerId, ownerName, request);

        // Then
        verify(emergencyContactRepository).findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED);
        verify(geocodingService).reverseGeocodeAddress(41.3874, 2.1686);
        verify(expoPushService, never()).send(anyList(), anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void trigger_withContactHavingNoTokens_skipsContact() {
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("Help!");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Barcelona, Spain");
        when(tokenService.listByUser(2L)).thenReturn(Collections.emptyList());

        service.trigger(ownerId, ownerName, request);

        verify(tokenService).listByUser(2L);
        verify(expoPushService, never()).send(anyList(), anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void trigger_withMultipleTokensForOneContact_sendsAllTokens() {
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("Help!");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token1 = new NotificationToken();
        token1.setToken("ExponentPushToken[token1]");
        NotificationToken token2 = new NotificationToken();
        token2.setToken("ExponentPushToken[token2]");
        List<NotificationToken> tokens = Arrays.asList(token1, token2);

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Barcelona, Spain");
        when(tokenService.listByUser(2L)).thenReturn(tokens);

        service.trigger(ownerId, ownerName, request);

        verify(expoPushService).send(argThat(list -> list.size() == 2), anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void trigger_includesCorrectDataInNotification() {
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("Help!");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token = new NotificationToken();
        token.setToken("ExponentPushToken[token1]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Barcelona, Spain");
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token));

        service.trigger(ownerId, ownerName, request);

        verify(expoPushService).send(
                anyList(),
                anyString(),
                anyString(),
                argThat(data -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    return dataMap.containsKey("type") &&
                            dataMap.containsKey("ownerId") &&
                            dataMap.containsKey("ownerName") &&
                            dataMap.containsKey("lat") &&
                            dataMap.containsKey("lng") &&
                            dataMap.containsKey("address") &&
                            dataMap.containsKey("timestamp") &&
                            dataMap.get("ownerId").equals(1L) &&
                            dataMap.get("ownerName").equals("John Doe") &&
                            dataMap.get("lat").equals(41.3874) &&
                            dataMap.get("lng").equals(2.1686);
                }),
                anyString()
        );
    }

    @Test
    void trigger_withMessageAndAddress_includesBothInBody() {
        // Given
        Long ownerId = 1L;
        String ownerName = "John Doe";
        EmergencyTriggerRequestDto request = new EmergencyTriggerRequestDto();
        request.setLatitude(41.3874);
        request.setLongitude(2.1686);
        request.setMessage("I need help urgently!");

        Set<EmergencyContact> contacts = Collections.singleton(emergencyContact1);

        NotificationToken token = new NotificationToken();
        token.setToken("ExponentPushToken[token1]");

        when(emergencyContactRepository.findByOwner_IdAndStatus(ownerId, EmergencyContactEnum.Status.ACCEPTED))
                .thenReturn(contacts);
        when(geocodingService.reverseGeocodeAddress(41.3874, 2.1686))
                .thenReturn("Barcelona, Spain");
        when(tokenService.listByUser(2L)).thenReturn(Collections.singletonList(token));

        service.trigger(ownerId, ownerName, request);

        verify(expoPushService).send(
                anyList(),
                anyString(),
                argThat(body -> body.contains("I need help urgently!") && body.contains("Barcelona, Spain")),
                anyMap(),
                anyString()
        );
    }
}
