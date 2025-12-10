package com.tfg.aegis.companionrequest;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.companionrequest.mapper.CompanionRequestMapper;
import com.tfg.aegis.companionrequest.mapper.CreateCompanionRequestMapper;
import com.tfg.aegis.companionrequest.model.CompanionRequest;
import com.tfg.aegis.companionrequest.model.CompanionRequestDto;
import com.tfg.aegis.companionrequest.model.CreateCompanionRequestDto;
import com.tfg.aegis.companionrequest.model.Enums;
import com.tfg.aegis.group.GroupRepository;
import com.tfg.aegis.journey.JourneyRepository;
import com.tfg.aegis.journey.model.JourneyDto;
import com.tfg.aegis.location.LocationRepository;
import com.tfg.aegis.location.mapper.LocationMapper;
import com.tfg.aegis.location.model.Location;
import com.tfg.aegis.person.user.UserRepository;
import com.tfg.aegis.person.user.UserService;
import com.tfg.aegis.person.user.mapper.UserMapper;
import com.tfg.aegis.person.user.model.User;
import com.tfg.aegis.person.user.model.UserDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final CompanionRequestMapper companionRequestMapper;
    private final CreateCompanionRequestMapper createCompanionRequestMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    private static final Logger log = LoggerFactory.getLogger(CompanionRequestService.class);


    /**
     * Endpoint for the creator
     */

    public Long createCompanionRequest(CreateCompanionRequestDto createCompanionRequestDto) {
        User currentUser = userRepository.findById(getCurrentUser().getId()).orElseThrow(() -> new NotFoundException("createCompanionRequest", getCurrentUser().getId()));

        CompanionRequest companionRequest = createCompanionRequestMapper.toEntity(createCompanionRequestDto);
        companionRequest.setCreator(currentUser);
        Location source = locationRepository.findById(createCompanionRequestDto.getSourceId())
                .orElseThrow(() -> new EntityNotFoundException("Ubicación de origen no encontrada: " + createCompanionRequestDto.getSourceId()));
        Location destination = locationRepository.findById(createCompanionRequestDto.getDestinationId())
                .orElseThrow(() -> new EntityNotFoundException("Ubicación de destino no encontrada: " + createCompanionRequestDto.getDestinationId()));
        companionRequest.setSource(source);
        companionRequest.setDestination(destination);
        companionRequest.setState(Enums.RequestStatus.CREATED);
        companionRequest.setCreationDate(LocalDateTime.now());
        CompanionRequest savedRequest = companionRequestRepository.save(companionRequest);
        log.info("Companion request created with id: {}", savedRequest.getId());
        return savedRequest.getId();
    }

    public CompanionRequestDto acceptCompanionRequest(Long requestId) {
        CompanionRequest request = companionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + requestId));

        User currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Solo el creador puede aceptar
        if (!request.getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Solo el creador puede aceptar la solicitud");
        }

        // Debe haber un acompañante pendiente
        if (request.getCompanion() == null) {
            throw new IllegalStateException("No hay ningún acompañante pendiente de aceptar");
        }

        if (request.getState() != Enums.RequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud no está pendiente de aceptación");
        }

        request.setState(Enums.RequestStatus.MATCHED);

        log.info("Companion request with id: {} has been accepted", requestId);

        return toDtoWithRelations(request);
    }

    public void rejectCompanionRequest(Long id) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        User currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!request.getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Solo el creador puede rechazar la solicitud");
        }

        if (request.getCompanion() == null || request.getState() != Enums.RequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud no está en un estado válido para ser rechazada");
        }

        request.setState(Enums.RequestStatus.CREATED);
        request.setCompanion(null);
        request.setCompanionMessage(null);
        log.info("Companion request with id: {} has been rejected", id);
    }


    public CompanionRequestDto submitIndividualJourney(Long id, JourneyDto journeyDto) {
        return null;
    }

    public CompanionRequestDto finishCompanionRequest(Long id) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        Long currentId = getCurrentUser().getId();
        boolean isCreator = request.getCreator().getId().equals(currentId);
        boolean isCompanion = request.getCompanion() != null
                && request.getCompanion().getId().equals(currentId);

        if (!isCreator && !isCompanion) {
            throw new IllegalStateException("Solo el creador o el acompañante pueden finalizar la solicitud");
        }

        if (request.getState() != Enums.RequestStatus.IN_PROGRESS
                && request.getState() != Enums.RequestStatus.MATCHED) {
            throw new IllegalStateException("La solicitud no está en marcha");
        }

        request.setState(Enums.RequestStatus.FINISHED);

        log.info("Finishing companion request with id: {}", id);

        return toDtoWithRelations(request);
    }

    public void deleteCompanionRequest(Long id) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        Long currentId = getCurrentUser().getId();
        if (!request.getCreator().getId().equals(currentId)) {
            throw new IllegalStateException("Solo el creador puede eliminar la solicitud");
        }

        if (request.getState() == Enums.RequestStatus.IN_PROGRESS
                || request.getState() == Enums.RequestStatus.FINISHED) {
            throw new IllegalStateException("No se puede eliminar una solicitud con trayecto en marcha o finalizado");
        }

        log.info("Deleting companion request with id: {}", id);

        companionRequestRepository.delete(request);
    }

    /**
     * Endpoint for the searchers
     */

    public List<CompanionRequestDto> getMyCompanionRequests() {
        Long userId = getCurrentUser().getId();
        List<CompanionRequest> requests = companionRequestRepository.findByCreatorIdOrCompanionId(userId, userId);
        return requests.stream()
                .map(this::toDtoWithRelations)
                .toList();
    }

    public List<CompanionRequestDto> listActiveCompanionRequests() {
        List<CompanionRequest> requests = companionRequestRepository.findByAproxHourBetween(LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(3));

        log.info("Mapped source, destination, and creator for active companion requests");
        return requests.stream()
                .filter(r -> r.getState() == Enums.RequestStatus.CREATED)
                .map(this::toDtoWithRelations)
                .toList();
    }

    public List<CompanionRequestDto> searchCompanionRequests(Long destinationId, LocalDateTime from, LocalDateTime to, boolean excludeMine) {
        List<CompanionRequest> requests;

        if (destinationId != null) {
            Location destination = locationRepository.findById(destinationId)
                    .orElseThrow(() -> new EntityNotFoundException("Destino no encontrado: " + destinationId));

            requests = companionRequestRepository.findByDestinationAndAproxHourBetween(destination, from, to);
        } else {
            if (from == null) {
                from = LocalDateTime.now().minusHours(2);
            }
            if (to == null) {
                to = LocalDateTime.now().plusHours(3);
            }
            requests = companionRequestRepository.findByAproxHourBetween(from, to);
        }

        if (excludeMine) {
            Long currentId = getCurrentUser().getId();
            requests = requests.stream()
                    .filter(r -> !r.getCreator().getId().equals(currentId))
                    .toList();
        }

        return requests.stream()
                .map(this::toDtoWithRelations)
                .toList();
    }

    public void requestToJoinCompanionRequest(Long id, String companionMessage) {
        /*
        Una vez el ambos usuarios (creador y acompañante) han acordado el viaje,
         */
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        User currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("No puedes unirte a tu propia solicitud");
        }
        if (request.getState() != Enums.RequestStatus.CREATED) {
            throw new IllegalStateException("La solicitud no está disponible para unirse");
        }
        if (request.getCompanion() != null) {
            throw new IllegalStateException("La solicitud ya tiene un acompañante pendiente o asignado");
        }

        // El acompañante se “postula” → queda pendiente de aceptación
        request.setCompanionMessage(companionMessage);
        request.setCompanion(currentUser);
        request.setState(Enums.RequestStatus.PENDING);
    }

    public void cancelCompanionRequest(Long requestId) {
        CompanionRequest request = companionRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + requestId));

        User currentUser = userRepository.findById(getCurrentUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getCompanion() == null
                || !request.getCompanion().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Solo el acompañante pendiente puede cancelar su solicitud de unión");
        }

        if (request.getState() != Enums.RequestStatus.PENDING) {
            throw new IllegalStateException("Solo puedes cancelar solicitudes pendientes de aceptación");
        }

        // Volvemos al estado inicial
        request.setCompanion(null);
        request.setCompanionMessage(null);
        request.setState(Enums.RequestStatus.CREATED);
    }

    /**
     * Endpoint for both the creator and the searcher
     */

    public CompanionRequestDto getCompanionRequestById(Long id) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));
        return toDtoWithRelations(request);
    }

    // Utility method to get the current authenticated user
    private UserDto getCurrentUser() {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByClerkId(clerkId);
    }

    private CompanionRequestDto toDtoWithRelations(CompanionRequest entity) {
        CompanionRequestDto dto = companionRequestMapper.toDto(entity);
        dto.setSource(entity.getSource() != null ? locationMapper.toDto(entity.getSource()) : null);
        dto.setDestination(entity.getDestination() != null ? locationMapper.toDto(entity.getDestination()) : null);
        dto.setCreator(entity.getCreator() != null ? userMapper.toDto(entity.getCreator()) : null);
        log.info("Mapped source {}, destination {}, and creator {} for companion request with id: {}",
                dto.getSource(), dto.getDestination(), dto.getCreator(), dto.getId());
        return dto;
    }

}
