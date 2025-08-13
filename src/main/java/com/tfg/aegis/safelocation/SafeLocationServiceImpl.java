package com.tfg.aegis.safelocation;

import com.tfg.aegis.exception.user.ResourceNotFoundException;
import com.tfg.aegis.mapper.SafeLocationMapper;
import com.tfg.aegis.safelocation.model.SafeLocation;
import com.tfg.aegis.safelocation.model.SafeLocationDto;
import com.tfg.aegis.user.UserRepository;
import com.tfg.aegis.user.UserServiceImpl;
import com.tfg.aegis.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SafeLocationServiceImpl implements SafeLocationService {

    private final SafeLocationRepository repository;

    private final UserRepository userRepository;

    private final UserServiceImpl userServiceImpl;

    private static final Logger log = LoggerFactory.getLogger(SafeLocationServiceImpl.class);

    public SafeLocationServiceImpl(SafeLocationRepository repository, UserRepository userRepository, UserServiceImpl userServiceImpl) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.userServiceImpl = userServiceImpl;
    }

    /**
     * Retrieves a SafeLocation by its ID and checks if the current user is the owner.
     */
    private SafeLocation getOwnedLocationOrThrow(Long locationId) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userServiceImpl.getUserByClerkId(clerkId);
        SafeLocation location = repository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("SafeLocation not found"));

        if (!location.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this SafeLocation");
        }

        log.info("User {} accessed SafeLocation with ID {}", user.getId(), location);
        return location;
    }

    /**
     * {@inheritDoc}
     */
    public Long addSafeLocationForCurrentUser(SafeLocationDto dto) {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with clerkId: " + clerkId));
        SafeLocation location = SafeLocationMapper.toEntity(dto, user);
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
}