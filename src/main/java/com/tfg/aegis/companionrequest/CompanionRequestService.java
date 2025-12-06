package com.tfg.aegis.companionrequest;

import com.tfg.aegis.companionrequest.mapper.CompanionRequestMapper;
import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.companionrequest.model.CompanionRequestDto;
import com.tfg.aegis.companionrequest.model.Enums;
import com.tfg.aegis.group.GroupRepository;
import com.tfg.aegis.group.GroupService;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.journey.JourneyRepository;
import com.tfg.aegis.journey.JourneyService;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.location.LocationRepository;
import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.person.user.UserRepository;
import com.tfg.aegis.person.user.UserService;
import com.tfg.aegis.person.user.model.User;
import com.tfg.aegis.person.user.model.UserDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanionRequestService {

    private final CompanionRequestRepository companionRequestRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final JourneyRepository journeyRepository;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final CompanionRequestMapper companionRequestMapper;
    private final GroupService groupService;
    private final JourneyService journeyService;

    public Long createCompanionRequest(CompanionRequestDto companionRequestDto) {
        User currentUser = userRepository.findById(getCurrentUser().getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        CompanionRequest companionRequest = companionRequestMapper.toEntity(companionRequestDto);
        companionRequest.setCreator(currentUser);
        companionRequest.setCreationDate(LocalDateTime.now());
        companionRequest.setState(Enums.RequestStatus.CREATED);
        CompanionRequest savedRequest = companionRequestRepository.save(companionRequest);
        return savedRequest.getId();
    }

    public List<CompanionRequestDto> searchCompanionRequests(Long destinationId, LocalDateTime from, LocalDateTime to, boolean excludeMine) {
        // TODO: Aplicar logica de que el destino puede ser proximo, no exacto

        Location destination = locationRepository.findById(destinationId)
                .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado: " + destinationId));

        List<CompanionRequest> requests = companionRequestRepository.findByDestinationAndAproxHourBetween(destination, from, to);

        if (excludeMine) {
            requests = requests.stream()
                    .filter(s -> !s.getCreator().getId().equals(getCurrentUser().getId()))
                    .toList();
        }

        return requests.stream().map(companionRequestMapper::toDto).toList();
    }

    public List<CompanionRequestDto> getMyCompanionRequests() {
        Long userId = getCurrentUser().getId();
        List<CompanionRequest> requests = companionRequestRepository.findByCreatorIdOrCompanionId(userId, userId);
        return requests.stream().map(companionRequestMapper::toDto).toList();
    }

    public CompanionRequestDto acceptCompanionRequest(Long requestId, GroupDto groupDto) {
        CompanionRequest request = companionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + requestId));

        User companion = userRepository.findById(getCurrentUser().getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getState() != Enums.RequestStatus.CREATED) {
            throw new IllegalStateException("La solicitud no está disponible para ser aceptada");
        }
        if (request.getCreator().getId().equals(companion.getId())) {
            throw new IllegalStateException("No puedes aceptar tu propia solicitud");
        }
        if (request.getCompanion() != null) {
            throw new IllegalStateException("La solicitud ya tiene acompañante");
        }

        request.setCompanion(companion);
        request.setState(Enums.RequestStatus.MATCHED);

        // Crear grupo companion (chat entre creador y acompañante)
        // Adapta esto a tu GroupService / enums reales
        Long groupId = groupService.createGroup(groupDto);
        Group companionGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + groupId));
        request.setCompanionGroup(companionGroup);

        return companionRequestMapper.toDto(request);
    }

    public void cancelCompanionRequest(Long requestId) {
        CompanionRequest request = companionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + requestId));

        if (!request.getCreator().getId().equals(getCurrentUser().getId())) {
            throw new IllegalStateException("Solo el creador puede cancelar la solicitud");
        }
        if (request.getState() == Enums.RequestStatus.IN_PROGRESS ||
                request.getState() == Enums.RequestStatus.FINISHED) {
            throw new IllegalStateException("No se puede cancelar una solicitud con trayecto en marcha o finalizado");
        }
        if (request.getState() == Enums.RequestStatus.CANCELLED) {
            throw new IllegalStateException("La solicitud ya está cancelada");
        }
        request.setState(Enums.RequestStatus.CANCELLED);
    }

    public void declineCompanionRequest(Long id) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        User currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getCompanion() == null || !request.getCompanion().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Solo el acompañante asignado puede rechazar la solicitud");
        }
        if (request.getState() != Enums.RequestStatus.MATCHED) {
            throw new IllegalStateException("La solicitud no está en un estado válido para ser rechazada");
        }

        request.setCompanion(null);
        request.setState(Enums.RequestStatus.DECLINED);
    }

    public CompanionRequestDto startJourney(Long requestId, JourneyDto journeyDto) {
        CompanionRequest request = companionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + requestId));

        User currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!request.getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Solo el creador puede iniciar el trayecto");
        }
        if (request.getState() != Enums.RequestStatus.MATCHED) {
            throw new IllegalStateException("La solicitud no está en un estado válido para iniciar el trayecto");
        }

        // Aquí deberías crear el Journey basado en journeyDto y asignarlo a la solicitud
        // Por simplicidad, este paso se omite
        // TODO: Implement journey creation logic: donde origen y destino son los de la solicitud
        Long journeyId = journeyService.createJourney(journeyDto);
        // Asignar el trayecto creado a la solicitud
        request.setTrayecto(journeyRepository.findById(journeyId)
                .orElseThrow(() -> new EntityNotFoundException("Trayecto no encontrado: " + journeyId)));

        request.setState(Enums.RequestStatus.IN_PROGRESS);
        return companionRequestMapper.toDto(request);
    }

    private UserDto getCurrentUser() {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByClerkId(clerkId);
    }
}
