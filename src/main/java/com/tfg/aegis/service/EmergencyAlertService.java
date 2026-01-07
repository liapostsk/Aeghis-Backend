package com.tfg.aegis.service;

import com.tfg.aegis.common.utils.Constants;
import com.tfg.aegis.model.dto.EmergencyTriggerRequestDto;
import com.tfg.aegis.model.entity.EmergencyContact;
import com.tfg.aegis.model.entity.NotificationToken;
import com.tfg.aegis.model.enums.EmergencyContactEnum;
import com.tfg.aegis.repository.EmergencyContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyAlertService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final NotificationTokenService tokenService;
    private final ExpoPushService expoPushService;
    private final GeocodingService geocodingService;

    @Transactional
    public void trigger(Long ownerId, String ownerName, EmergencyTriggerRequestDto req) {

        Set<EmergencyContact> contacts =
                emergencyContactRepository.findByOwner_IdAndStatus(
                        ownerId,
                        EmergencyContactEnum.Status.ACCEPTED
                );

        String address = geocodingService.reverseGeocodeAddress(
                req.getLatitude(),
                req.getLongitude()
        );

        String title = Constants.EMERGENCIA;
        String body = (req.getMessage() != null && !req.getMessage().isBlank())
                ? req.getMessage()
                : (ownerName + Constants.MESSAGE);
        if (address != null && !address.isBlank()) {
            body += Constants.LOCATION_EMOJI + address;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("type", Constants.NOTIFICATION_TYPE_EMERGENCY);
        data.put("ownerId", ownerId);
        data.put("ownerName", ownerName);
        data.put("lat", req.getLatitude());
        data.put("lng", req.getLongitude());
        data.put("address", address);
        data.put("timestamp", System.currentTimeMillis());

        for (EmergencyContact ec : contacts) {
            Long contactUserId = ec.getContact().getId();

            List<String> tokens = tokenService
                    .listByUser(contactUserId)
                    .stream()
                    .map(NotificationToken::getToken)
                    .collect(Collectors.toList());

            if (!tokens.isEmpty()) {
                expoPushService.send(tokens, title, body, data, Constants.NOTIFICATION_TYPE_EMERGENCY);
            }
        }
    }
}
