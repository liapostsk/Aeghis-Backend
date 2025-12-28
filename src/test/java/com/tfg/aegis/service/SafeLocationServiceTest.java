package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.model.dto.SafeLocationDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.entity.SafeLocation;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.mapper.SafeLocationMapper;
import com.tfg.aegis.repository.SafeLocationRepository;
import com.tfg.aegis.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SafeLocationServiceTest {

    @Mock
    private SafeLocationRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private SafeLocationMapper mapper;

    @InjectMocks
    private SafeLocationService service;

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    /* =========================
     * addSafeLocationForCurrentUser
     * ========================= */
    @Test
    void addSafeLocationForCurrentUser_ok_createsAndReturnsId() {
        // Setup SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_123");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        SafeLocationDto dto = new SafeLocationDto();
        dto.setName("Home");
        dto.setLatitude(41.3874);
        dto.setLongitude(2.1686);

        User user = new User();
        user.setId(1L);
        user.setClerkId("clerk_123");

        SafeLocation location = new SafeLocation();
        location.setId(10L);
        location.setName("Home");
        location.setOwner(user);

        when(userRepository.findByClerkId("clerk_123")).thenReturn(Optional.of(user));
        when(mapper.toEntity(dto, user)).thenReturn(location);
        when(repository.save(location)).thenReturn(location);

        Long result = service.addSafeLocationForCurrentUser(dto);

        assertEquals(10L, result);
        verify(userRepository).findByClerkId("clerk_123");
        verify(mapper).toEntity(dto, user);
        verify(repository).save(location);
    }

    @Test
    void addSafeLocationForCurrentUser_userNotFound_throwsNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_invalid");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        SafeLocationDto dto = new SafeLocationDto();

        when(userRepository.findByClerkId("clerk_invalid")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.addSafeLocationForCurrentUser(dto));

        assertTrue(ex.getMessage().contains("User not found with clerkId"));
        verify(repository, never()).save(any());
    }

    /* =========================
     * editSafeLocationForCurrentUser
     * ========================= */
    @Test
    void editSafeLocationForCurrentUser_ok_updatesAllFields() {
        // Setup SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_owner");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Long locationId = 5L;
        SafeLocationDto dto = new SafeLocationDto();
        dto.setName("New Name");
        dto.setDescription("New Description");
        dto.setLatitude(40.4168);
        dto.setLongitude(-3.7038);
        dto.setAddress("Madrid, Spain");
        dto.setDistance("10 km");
        dto.setType("Office");

        User owner = new User();
        owner.setId(1L);

        UserDto ownerDto = new UserDto();
        ownerDto.setId(1L);

        SafeLocation location = new SafeLocation();
        location.setId(locationId);
        location.setOwner(owner);
        location.setName("Old Name");

        when(userService.getUserByClerkId("clerk_owner")).thenReturn(ownerDto);
        when(repository.findById(locationId)).thenReturn(Optional.of(location));

        service.editSafeLocationForCurrentUser(locationId, dto);

        assertEquals("New Name", location.getName());
        assertEquals("New Description", location.getDescription());
        assertEquals(40.4168, location.getLatitude());
        assertEquals(-3.7038, location.getLongitude());
        assertEquals("Madrid, Spain", location.getAddress());
        assertEquals("10 km", location.getDistance());
        assertEquals("Office", location.getType());

        verify(repository).save(location);
    }

    @Test
    void editSafeLocationForCurrentUser_locationNotFound_throwsNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_user");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        SafeLocationDto dto = new SafeLocationDto();

        UserDto userDto = new UserDto();
        userDto.setId(1L);

        when(userService.getUserByClerkId("clerk_user")).thenReturn(userDto);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.editSafeLocationForCurrentUser(999L, dto));

        verify(repository, never()).save(any());
    }

    @Test
    void editSafeLocationForCurrentUser_notOwner_throwsAccessDenied() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_other");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Long locationId = 5L;
        SafeLocationDto dto = new SafeLocationDto();

        User owner = new User();
        owner.setId(1L);

        UserDto otherUser = new UserDto();
        otherUser.setId(2L); // Different user

        SafeLocation location = new SafeLocation();
        location.setId(locationId);
        location.setOwner(owner);

        when(userService.getUserByClerkId("clerk_other")).thenReturn(otherUser);
        when(repository.findById(locationId)).thenReturn(Optional.of(location));

        assertThrows(AccessDeniedException.class,
                () -> service.editSafeLocationForCurrentUser(locationId, dto));

        verify(repository, never()).save(any());
    }

    /* =========================
     * deleteSafeLocationForCurrentUser
     * ========================= */
    @Test
    void deleteSafeLocationForCurrentUser_ok_deletesSuccessfully() {
        Long locationId = 10L;

        when(repository.existsById(locationId))
                .thenReturn(true)  // existed before
                .thenReturn(false); // doesn't exist after delete

        service.deleteSafeLocationForCurrentUser(locationId);

        verify(repository).deleteById(locationId);
        verify(repository, times(2)).existsById(locationId);
    }

    @Test
    void deleteSafeLocationForCurrentUser_locationNotExisted_deletesWithoutError() {
        Long locationId = 20L;

        when(repository.existsById(locationId))
                .thenReturn(false); // didn't exist before

        service.deleteSafeLocationForCurrentUser(locationId);

        verify(repository).deleteById(locationId);
        verify(repository, times(1)).existsById(locationId); // Only checked once (before delete)
    }

    @Test
    void deleteSafeLocationForCurrentUser_deleteFails_throwsIllegalState() {
        Long locationId = 30L;

        when(repository.existsById(locationId))
                .thenReturn(true)  // existed before
                .thenReturn(true); // still exists after delete (deletion failed)

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.deleteSafeLocationForCurrentUser(locationId));

        assertTrue(ex.getMessage().contains("No se borró el SafeLocation"));
        assertTrue(ex.getMessage().contains("id=" + locationId));

        verify(repository).deleteById(locationId);
    }

    /* =========================
     * getOwnedLocationOrThrow (tested indirectly through editSafeLocationForCurrentUser)
     * ========================= */
    @Test
    void getOwnedLocationOrThrow_validOwner_returnsLocation() {
        // This is tested indirectly through editSafeLocationForCurrentUser_ok_updatesAllFields
        // The method is private, so we test it through public methods
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_valid");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Long locationId = 1L;
        User owner = new User();
        owner.setId(1L);

        UserDto ownerDto = new UserDto();
        ownerDto.setId(1L);

        SafeLocation location = new SafeLocation();
        location.setId(locationId);
        location.setOwner(owner);

        SafeLocationDto dto = new SafeLocationDto();
        dto.setName("Test");

        when(userService.getUserByClerkId("clerk_valid")).thenReturn(ownerDto);
        when(repository.findById(locationId)).thenReturn(Optional.of(location));

        // Should not throw any exception
        assertDoesNotThrow(() -> service.editSafeLocationForCurrentUser(locationId, dto));
    }

    @Test
    void getOwnedLocationOrThrow_ownerIdNull_throwsNullPointer() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_user");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        Long locationId = 1L;
        User owner = new User();
        owner.setId(null); // Owner ID is null

        UserDto userDto = new UserDto();
        userDto.setId(1L);

        SafeLocation location = new SafeLocation();
        location.setId(locationId);
        location.setOwner(owner);

        SafeLocationDto dto = new SafeLocationDto();

        when(userService.getUserByClerkId("clerk_user")).thenReturn(userDto);
        when(repository.findById(locationId)).thenReturn(Optional.of(location));

        // Should throw NullPointerException because owner.getId() is null
        assertThrows(NullPointerException.class,
                () -> service.editSafeLocationForCurrentUser(locationId, dto));
    }
}

