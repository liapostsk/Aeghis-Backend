package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.model.mapper.CompanionRequestMapper;
import com.tfg.aegis.model.mapper.CreateCompanionRequestMapper;
import com.tfg.aegis.model.entity.CompanionRequest;
import com.tfg.aegis.model.dto.CompanionRequestDto;
import com.tfg.aegis.model.dto.CreateCompanionRequestDto;
import com.tfg.aegis.model.enums.CompanionRequestEnums;
import com.tfg.aegis.repository.CompanionRequestRepository;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.dto.JourneyDto;
import com.tfg.aegis.repository.LocationRepository;
import com.tfg.aegis.model.mapper.LocationMapper;
import com.tfg.aegis.model.entity.Location;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.mapper.UserMapper;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
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
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final CompanionRequestMapper companionRequestMapper;
    private final CreateCompanionRequestMapper createCompanionRequestMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    private static final Logger log = LoggerFactory.getLogger(CompanionRequestService.class);


    /**
     * Endpoint for the creator
     */

    /**
     * Create a companion request
     * @param createCompanionRequestDto
     * @return
     */
    public Long createCompanionRequest(CreateCompanionRequestDto createCompanionRequestDto) {
        User currentUser = userRepository.findById(getCurrentUser().getId()).orElseThrow(() -> new NotFoundException("createCompanionRequest", getCurrentUser().getId()));
        if (userHasActiveRequest(currentUser.getId())) {
            throw new IllegalStateException(
                    "Ya tienes una solicitud de acompañamiento en estado CREATED. " +
                            "Cancélala o edítala antes de crear una nueva."
            );
        }
        CompanionRequest companionRequest = createCompanionRequestMapper.toEntity(createCompanionRequestDto);
        companionRequest.setCreator(currentUser);
        Location source = locationRepository.findById(createCompanionRequestDto.getSourceId())
                .orElseThrow(() -> new EntityNotFoundException("Ubicación de origen no encontrada: " + createCompanionRequestDto.getSourceId()));
        Location destination = locationRepository.findById(createCompanionRequestDto.getDestinationId())
                .orElseThrow(() -> new EntityNotFoundException("Ubicación de destino no encontrada: " + createCompanionRequestDto.getDestinationId()));
        companionRequest.setSource(source);
        companionRequest.setDestination(destination);
        companionRequest.setState(CompanionRequestEnums.RequestStatus.CREATED);
        companionRequest.setCreationDate(LocalDateTime.now());
        CompanionRequest savedRequest = companionRequestRepository.save(companionRequest);
        log.info("Companion request created with id: {}", savedRequest.getId());
        return savedRequest.getId();
    }

    /**
     * Edit a companion request
     * @param id
     * @param createCompanionRequestDto
     * @return
     */
    public CompanionRequestDto editCompanionRequest(Long id, CreateCompanionRequestDto createCompanionRequestDto) {
        CompanionRequest request = companionRequestRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        Long currentId = getCurrentUser().getId();
        if (!request.getCreator().getId().equals(currentId)) {
            throw new IllegalStateException("Solo el creador puede editar la solicitud");
        }

        if (request.getState() != CompanionRequestEnums.RequestStatus.CREATED) {
            throw new IllegalStateException("Solo se pueden editar solicitudes en estado CREADA");
        }

        Location source = locationRepository.findById(createCompanionRequestDto.getSourceId()).orElseThrow(() -> new EntityNotFoundException("Ubicación de origen no encontrada: " + createCompanionRequestDto.getSourceId()));
        Location destination = locationRepository.findById(createCompanionRequestDto.getDestinationId()).orElseThrow(() -> new EntityNotFoundException("Ubicación de destino no encontrada: " + createCompanionRequestDto.getDestinationId()));

        request.setSource(source);
        request.setDestination(destination);
        request.setAproxHour(createCompanionRequestDto.getAproxHour());
        request.setDescription(createCompanionRequestDto.getDescription());

        log.info("Companion request with id: {} has been edited", id);

        return toDtoWithRelations(request);
    }

    /**
     * Link a group to a companion request
     * @param id
     * @param groupId
     * @return
     */
    public CompanionRequestDto linkGroupToCompanionRequest(Long id, Long groupId) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        Long currentId = getCurrentUser().getId();
        if (!request.getCreator().getId().equals(currentId)) {
            throw new IllegalStateException("Solo el creador puede vincular un grupo a la solicitud");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + groupId));

        request.setCompanionGroup(group);

        log.info("Linking group with id: {} to companion request with id: {}", groupId, id);

        return toDtoWithRelations(request);
    }

    /**
     * Link a tracking group to a companion request
     * @param id
     * @param groupId
     * @return
     */
    public CompanionRequestDto linkTrackingGroupToCompanionRequest(Long id, Long groupId, Boolean isCreatorGroup) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        Long currentId = getCurrentUser().getId();
        if (!request.getCreator().getId().equals(currentId)) {
            throw new IllegalStateException("Solo el creador puede vincular un grupo de seguimiento a la solicitud");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + groupId));

        if (isCreatorGroup) {
            request.setCreatorTrackingGroup(group);
            log.info("Linking creator tracking group with id: {} to companion request with id: {}", groupId, id);
        } else {
            request.setCompanionTrackingGroup(group);
            log.info("Linking companion tracking group with id: {} to companion request with id: {}", groupId, id);
        }

        return toDtoWithRelations(request);
    }

    /**
     * Accept a companion request
     * @param requestId
     * @return
     */
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

        if (request.getState() != CompanionRequestEnums.RequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud no está pendiente de aceptación");
        }

        request.setState(CompanionRequestEnums.RequestStatus.MATCHED);

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

        if (request.getCompanion() == null || request.getState() != CompanionRequestEnums.RequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud no está en un estado válido para ser rechazada");
        }

        request.setState(CompanionRequestEnums.RequestStatus.CREATED);
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

        if (request.getState() != CompanionRequestEnums.RequestStatus.IN_PROGRESS
                && request.getState() != CompanionRequestEnums.RequestStatus.MATCHED) {
            throw new IllegalStateException("La solicitud no está en marcha");
        }

        request.setState(CompanionRequestEnums.RequestStatus.FINISHED);

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

        if (request.getState() == CompanionRequestEnums.RequestStatus.IN_PROGRESS
                || request.getState() == CompanionRequestEnums.RequestStatus.FINISHED) {
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

        requests.forEach(this::updateExpiredStateIfNeeded);

        return requests.stream()
                .map(this::toDtoWithRelations)
                .toList();
    }

    public List<CompanionRequestDto> listActiveCompanionRequests() {
        List<CompanionRequest> requests = companionRequestRepository
                .findByAproxHourBetween(LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(3));

        requests.forEach(this::updateExpiredStateIfNeeded);

        List<CompanionRequestDto> result = requests.stream()
                .filter(r -> r.getState() == CompanionRequestEnums.RequestStatus.CREATED || r.getState() == CompanionRequestEnums.RequestStatus.PENDING)
                .map(this::toDtoWithRelations)
                .toList();

        log.info("Returning {} active companion requests (CREATED/PENDING)", result.size());

        return result;
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

        requests.forEach(this::updateExpiredStateIfNeeded);

        return requests.stream()
                .map(this::toDtoWithRelations)
                .toList();
    }

    public void requestToJoinCompanionRequest(Long id, String companionMessage) {
        /*
        Una vez el ambos usuarios (creador y acompañante) han acordado el viaje,
         */
        try {
            CompanionRequest request = companionRequestRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

            User currentUser = userRepository.findById(getCurrentUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (request.getCreator().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("No puedes unirte a tu propia solicitud");
            }

            if (request.getState() != CompanionRequestEnums.RequestStatus.CREATED) {
                throw new IllegalStateException("La solicitud no está disponible para unirse");
            }

            if (request.getCompanion() != null) {
                throw new IllegalStateException("La solicitud ya tiene un acompañante pendiente o asignado");
            }

            // Guardar mensaje del acompañante
            request.setCompanionMessage(companionMessage);
            request.setCompanion(currentUser);
            request.setState(CompanionRequestEnums.RequestStatus.PENDING);

            log.info("User {} requested to join companion request {}", currentUser.getId(), id);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            log.error("Error al unirse a companion request {}: {}", id, e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.warn("Estado inválido al intentar unirse a companion request {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al unirse a companion request {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("No se pudo procesar la solicitud de unión");
        }
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

        if (request.getState() != CompanionRequestEnums.RequestStatus.PENDING) {
            throw new IllegalStateException("Solo puedes cancelar solicitudes pendientes de aceptación");
        }

        // Volvemos al estado inicial
        request.setCompanion(null);
        request.setCompanionMessage(null);
        request.setState(CompanionRequestEnums.RequestStatus.CREATED);
    }

    /**
     * Endpoint for both the creator and the searcher
     */

    public CompanionRequestDto getCompanionRequestById(Long id) {
        CompanionRequest request = companionRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada: " + id));

        updateExpiredStateIfNeeded(request);

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
        dto.setCompanion(entity.getCompanion() != null ? userMapper.toDto(entity.getCompanion()) : null);
        dto.setCompanionGroupId(entity.getCompanionGroup() != null ? entity.getCompanionGroup().getId() : null);
        dto.setCreatorTrackingGroup(entity.getCreatorTrackingGroup() != null ? entity.getCreatorTrackingGroup().getId() : null);
        dto.setCompanionTrackingGroup(entity.getCompanionTrackingGroup() != null ? entity.getCompanionTrackingGroup().getId() : null);
        dto.setTrayectoId(entity.getTrayecto() != null ? entity.getTrayecto().getId() : null);
        log.info("Mapped source {}, destination {}, and creator {}, and companion {} for companion request with id: {}",
                dto.getSource(), dto.getDestination(), dto.getCreator(), dto.getCompanion() ,dto.getId());
        return dto;
    }

    private void updateExpiredStateIfNeeded(CompanionRequest request) {
        if (request.getAproxHour() == null) return;

        LocalDateTime now = LocalDateTime.now();

        boolean isExpirable =
                request.getState() == CompanionRequestEnums.RequestStatus.CREATED; // Solo las solicitudes CREADAS pueden expirar

        if (isExpirable && request.getAproxHour().isBefore(now)) {
            request.setState(CompanionRequestEnums.RequestStatus.EXPIRED);
            log.info("Companion request {} marcada como EXPIRED (aproxHour={}, now={})",
                    request.getId(), request.getAproxHour(), now);
        }
    }

    private boolean userHasActiveRequest(Long userId) {
        return companionRequestRepository.existsByCreatorIdAndState(
                userId,
                CompanionRequestEnums.RequestStatus.CREATED
        );
    }

    public CompanionRequestDto getCompanionRequestByCompanionGroupId(Long groupId) {
        CompanionRequest request = companionRequestRepository.findByCompanionGroupId(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada para el grupo de acompañantes: " + groupId));

        updateExpiredStateIfNeeded(request);

        return toDtoWithRelations(request);
    }
}
