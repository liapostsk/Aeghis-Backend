package com.tfg.aegis.service;

import com.tfg.aegis.model.mapper.SafeLocationMapper;
import com.tfg.aegis.repository.SafeLocationRepository;
import com.tfg.aegis.model.entity.SafeLocation;
import com.tfg.aegis.model.dto.SafeLocationDto;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.common.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class SafeLocationService {

    private final SafeLocationRepository repository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SafeLocationMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(SafeLocationService.class);

    /**
     * Retrieves a SafeLocation by its ID and checks if the current user is the owner.
     */
    private SafeLocation getOwnedLocationOrThrow(Long locationId) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDto userDto = userService.getUserByClerkId(clerkId);
        SafeLocation location = repository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("SafeLocation not found"));

        if (!location.getOwner().getId().equals(userDto.getId())) {
            throw new AccessDeniedException("You do not own this SafeLocation");
        }

        log.info("User {} accessed SafeLocation with ID {}", userDto.getId(), location);
        return location;
    }

    /**
     * {@inheritDoc}
     */
    public Long addSafeLocationForCurrentUser(SafeLocationDto dto) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new NotFoundException("User not found with clerkId: " + clerkId));
        SafeLocation location = mapper.toEntity(dto, user);
        repository.save(location);
        return location.getId();
    }

    /**
     * {@inheritDoc}
     */
    public void editSafeLocationForCurrentUser(Long id, SafeLocationDto dto) {
        SafeLocation location = getOwnedLocationOrThrow(id);
        location.setName(dto.getName());
        location.setDescription(dto.getDescription());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setAddress(dto.getAddress());
        location.setDistance(dto.getDistance());
        location.setType(dto.getType());

        repository.save(location);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    public void deleteSafeLocationForCurrentUser(Long id) {
        boolean existed = repository.existsById(id);
        repository.deleteById(id);
        if (existed && repository.existsById(id)) {
            throw new IllegalStateException("No se borró el SafeLocation id=" + id);
        }
    }

    public SafeLocationDto getSafeLocationForCurrentUser(Long id) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDto userDto = userService.getUserByClerkId(clerkId);
        SafeLocation location = getOwnedLocationOrThrow(id);
        return mapper.toDto(location, userDto);
    }
}