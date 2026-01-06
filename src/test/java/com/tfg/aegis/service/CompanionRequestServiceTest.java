package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.model.dto.CompanionRequestDto;
import com.tfg.aegis.model.dto.CreateCompanionRequestDto;
import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.entity.*;
import com.tfg.aegis.model.enums.CompanionRequestEnums;
import com.tfg.aegis.model.mapper.CompanionRequestMapper;
import com.tfg.aegis.model.mapper.CreateCompanionRequestMapper;
import com.tfg.aegis.model.mapper.LocationMapper;
import com.tfg.aegis.model.mapper.UserMapper;
import com.tfg.aegis.repository.CompanionRequestRepository;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.repository.LocationRepository;
import com.tfg.aegis.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanionRequestServiceTest {

    @Mock
    private CompanionRequestRepository companionRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserService userService;

    @Mock
    private CompanionRequestMapper companionRequestMapper;

    @Mock
    private CreateCompanionRequestMapper createCompanionRequestMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CompanionRequestService companionRequestService;

    private User creator;
    private User companion;
    private Location source;
    private Location destination;
    private CompanionRequest companionRequest;
    private CreateCompanionRequestDto createDto;
    private UserDto userDto;
    private Group group;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setClerkId("clerk_creator");

        companion = new User();
        companion.setId(2L);
        companion.setClerkId("clerk_companion");

        source = new Location();
        source.setId(1L);
        source.setLatitude(41.3851);
        source.setLongitude(2.1734);

        destination = new Location();
        destination.setId(2L);
        destination.setLatitude(41.3879);
        destination.setLongitude(2.1699);

        companionRequest = new CompanionRequest();
        companionRequest.setId(1L);
        companionRequest.setCreator(creator);
        companionRequest.setSource(source);
        companionRequest.setDestination(destination);
        companionRequest.setState(CompanionRequestEnums.RequestStatus.CREATED);
        companionRequest.setAproxHour(LocalDateTime.now().plusHours(1));
        companionRequest.setDescription("Test request");

        createDto = new CreateCompanionRequestDto();
        createDto.setSourceId(1L);
        createDto.setDestinationId(2L);
        createDto.setAproxHour(LocalDateTime.now().plusHours(1));
        createDto.setDescription("Test request");

        userDto = new UserDto();
        userDto.setId(1L);

        group = new Group();
        group.setId(1L);

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn("clerk_creator");
        lenient().when(userService.getUserByClerkId("clerk_creator")).thenReturn(userDto);

        // Mock mappers
        lenient().when(locationMapper.toDto(any())).thenReturn(new com.tfg.aegis.model.dto.LocationDto());
        lenient().when(userMapper.toDto(any())).thenReturn(new UserDto());
    }

    @Test
    void createCompanionRequest_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(companionRequestRepository.existsByCreatorIdAndState(1L, CompanionRequestEnums.RequestStatus.CREATED))
                .thenReturn(false);
        when(createCompanionRequestMapper.toEntity(any())).thenReturn(companionRequest);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(companionRequestRepository.save(any())).thenReturn(companionRequest);

        Long result = companionRequestService.createCompanionRequest(createDto);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(companionRequestRepository).save(any(CompanionRequest.class));
    }

    @Test
    void createCompanionRequest_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                companionRequestService.createCompanionRequest(createDto));
    }

    @Test
    void createCompanionRequest_userHasActiveRequest_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(companionRequestRepository.existsByCreatorIdAndState(1L, CompanionRequestEnums.RequestStatus.CREATED))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.createCompanionRequest(createDto));
    }

    @Test
    void createCompanionRequest_sourceNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(companionRequestRepository.existsByCreatorIdAndState(1L, CompanionRequestEnums.RequestStatus.CREATED))
                .thenReturn(false);
        when(createCompanionRequestMapper.toEntity(any())).thenReturn(companionRequest);
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                companionRequestService.createCompanionRequest(createDto));
    }

    @Test
    void createCompanionRequest_destinationNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(companionRequestRepository.existsByCreatorIdAndState(1L, CompanionRequestEnums.RequestStatus.CREATED))
                .thenReturn(false);
        when(createCompanionRequestMapper.toEntity(any())).thenReturn(companionRequest);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                companionRequestService.createCompanionRequest(createDto));
    }

    @Test
    void editCompanionRequest_success() {
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(source));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        CompanionRequestDto result = companionRequestService.editCompanionRequest(1L, createDto);

        assertNotNull(result);
        verify(companionRequestRepository).findById(1L);
    }

    @Test
    void editCompanionRequest_notCreator_throwsException() {
        companionRequest.setCreator(companion);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.editCompanionRequest(1L, createDto));
    }

    @Test
    void editCompanionRequest_notInCreatedState_throwsException() {
        companionRequest.setState(CompanionRequestEnums.RequestStatus.PENDING);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.editCompanionRequest(1L, createDto));
    }

    @Test
    void linkGroupToCompanionRequest_success() {
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        CompanionRequestDto result = companionRequestService.linkGroupToCompanionRequest(1L, 1L);

        assertNotNull(result);
        verify(groupRepository).findById(1L);
    }

    @Test
    void linkGroupToCompanionRequest_notCreator_throwsException() {
        companionRequest.setCreator(companion);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.linkGroupToCompanionRequest(1L, 1L));
    }

    @Test
    void deleteCompanionRequest_success() {
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        companionRequestService.deleteCompanionRequest(1L);

        verify(companionRequestRepository).delete(companionRequest);
    }

    @Test
    void deleteCompanionRequest_notCreator_throwsException() {
        companionRequest.setCreator(companion);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.deleteCompanionRequest(1L));
    }

    @Test
    void deleteCompanionRequest_inProgress_throwsException() {
        companionRequest.setState(CompanionRequestEnums.RequestStatus.IN_PROGRESS);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.deleteCompanionRequest(1L));
    }

    @Test
    void deleteCompanionRequest_finished_throwsException() {
        companionRequest.setState(CompanionRequestEnums.RequestStatus.FINISHED);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.deleteCompanionRequest(1L));
    }

    @Test
    void getMyCompanionRequests_success() {
        List<CompanionRequest> requests = Collections.singletonList(companionRequest);
        when(companionRequestRepository.findByCreatorIdOrCompanionId(1L, 1L)).thenReturn(requests);
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        List<CompanionRequestDto> result = companionRequestService.getMyCompanionRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void listActiveCompanionRequests_success() {
        List<CompanionRequest> requests = Collections.singletonList(companionRequest);
        when(companionRequestRepository.findByAproxHourBetween(any(), any())).thenReturn(requests);
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        List<CompanionRequestDto> result = companionRequestService.listActiveCompanionRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void listActiveCompanionRequests_filtersNonActiveStates() {
        CompanionRequest finishedRequest = new CompanionRequest();
        finishedRequest.setId(2L);
        finishedRequest.setState(CompanionRequestEnums.RequestStatus.FINISHED);
        finishedRequest.setAproxHour(LocalDateTime.now().plusHours(1));

        List<CompanionRequest> requests = List.of(companionRequest, finishedRequest);
        when(companionRequestRepository.findByAproxHourBetween(any(), any())).thenReturn(requests);
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        List<CompanionRequestDto> result = companionRequestService.listActiveCompanionRequests();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchCompanionRequests_withDestination_success() {
        List<CompanionRequest> requests = Collections.singletonList(companionRequest);
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(companionRequestRepository.findByDestinationAndAproxHourBetween(any(), any(), any()))
                .thenReturn(requests);
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        List<CompanionRequestDto> result = companionRequestService.searchCompanionRequests(
                2L, LocalDateTime.now(), LocalDateTime.now().plusHours(3), false);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchCompanionRequests_withoutDestination_success() {
        List<CompanionRequest> requests = Collections.singletonList(companionRequest);
        when(companionRequestRepository.findByAproxHourBetween(any(), any())).thenReturn(requests);
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        List<CompanionRequestDto> result = companionRequestService.searchCompanionRequests(
                null, LocalDateTime.now(), LocalDateTime.now().plusHours(3), false);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchCompanionRequests_excludeMine_success() {
        List<CompanionRequest> requests = Collections.singletonList(companionRequest);
        when(companionRequestRepository.findByAproxHourBetween(any(), any())).thenReturn(requests);
        lenient().when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        List<CompanionRequestDto> result = companionRequestService.searchCompanionRequests(
                null, LocalDateTime.now(), LocalDateTime.now().plusHours(3), true);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void requestToJoinCompanionRequest_success() {
        userDto.setId(2L);
        when(authentication.getPrincipal()).thenReturn("clerk_companion");
        when(userService.getUserByClerkId("clerk_companion")).thenReturn(userDto);

        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(companion));

        companionRequestService.requestToJoinCompanionRequest(1L, "I want to join");

        assertEquals(CompanionRequestEnums.RequestStatus.PENDING, companionRequest.getState());
        assertEquals(companion, companionRequest.getCompanion());
        assertEquals("I want to join", companionRequest.getCompanionMessage());
    }

    @Test
    void requestToJoinCompanionRequest_ownRequest_throwsException() {
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.requestToJoinCompanionRequest(1L, "Test"));
    }

    @Test
    void requestToJoinCompanionRequest_notCreatedState_throwsException() {
        userDto.setId(2L);
        when(authentication.getPrincipal()).thenReturn("clerk_companion");
        when(userService.getUserByClerkId("clerk_companion")).thenReturn(userDto);

        companionRequest.setState(CompanionRequestEnums.RequestStatus.PENDING);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(companion));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.requestToJoinCompanionRequest(1L, "Test"));
    }

    @Test
    void requestToJoinCompanionRequest_alreadyHasCompanion_throwsException() {
        userDto.setId(2L);
        when(authentication.getPrincipal()).thenReturn("clerk_companion");
        when(userService.getUserByClerkId("clerk_companion")).thenReturn(userDto);

        companionRequest.setCompanion(companion);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(companion));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.requestToJoinCompanionRequest(1L, "Test"));
    }

    @Test
    void cancelCompanionRequest_success() {
        userDto.setId(2L);
        when(authentication.getPrincipal()).thenReturn("clerk_companion");
        when(userService.getUserByClerkId("clerk_companion")).thenReturn(userDto);

        companionRequest.setState(CompanionRequestEnums.RequestStatus.PENDING);
        companionRequest.setCompanion(companion);
        companionRequest.setCompanionMessage("Test message");
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(companion));

        companionRequestService.cancelCompanionRequest(1L);

        assertEquals(CompanionRequestEnums.RequestStatus.CREATED, companionRequest.getState());
        assertNull(companionRequest.getCompanion());
        assertNull(companionRequest.getCompanionMessage());
    }

    @Test
    void cancelCompanionRequest_notCompanion_throwsException() {
        companionRequest.setState(CompanionRequestEnums.RequestStatus.PENDING);
        companionRequest.setCompanion(companion);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.cancelCompanionRequest(1L));
    }

    @Test
    void cancelCompanionRequest_notPendingState_throwsException() {
        userDto.setId(2L);
        when(authentication.getPrincipal()).thenReturn("clerk_companion");
        when(userService.getUserByClerkId("clerk_companion")).thenReturn(userDto);

        companionRequest.setState(CompanionRequestEnums.RequestStatus.CREATED);
        companionRequest.setCompanion(companion);
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(userRepository.findById(2L)).thenReturn(Optional.of(companion));

        assertThrows(IllegalStateException.class, () ->
                companionRequestService.cancelCompanionRequest(1L));
    }

    @Test
    void getCompanionRequestById_success() {
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        CompanionRequestDto result = companionRequestService.getCompanionRequestById(1L);

        assertNotNull(result);
        verify(companionRequestRepository).findById(1L);
    }

    @Test
    void getCompanionRequestById_notFound_throwsException() {
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                companionRequestService.getCompanionRequestById(1L));
    }

    @Test
    void getCompanionRequestById_expired_updatesState() {
        companionRequest.setAproxHour(LocalDateTime.now().minusHours(1));
        when(companionRequestRepository.findById(1L)).thenReturn(Optional.of(companionRequest));
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        CompanionRequestDto result = companionRequestService.getCompanionRequestById(1L);

        assertNotNull(result);
        assertEquals(CompanionRequestEnums.RequestStatus.EXPIRED, companionRequest.getState());
    }

    @Test
    void getCompanionRequestByCompanionGroupId_success() {
        companionRequest.setCompanionGroup(group);
        when(companionRequestRepository.findByCompanionGroupId(1L)).thenReturn(Optional.of(companionRequest));
        when(companionRequestMapper.toDto(any())).thenReturn(new CompanionRequestDto());

        CompanionRequestDto result = companionRequestService.getCompanionRequestByCompanionGroupId(1L);

        assertNotNull(result);
        verify(companionRequestRepository).findByCompanionGroupId(1L);
    }

    @Test
    void getCompanionRequestByCompanionGroupId_notFound_throwsException() {
        when(companionRequestRepository.findByCompanionGroupId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                companionRequestService.getCompanionRequestByCompanionGroupId(1L));
    }
}
