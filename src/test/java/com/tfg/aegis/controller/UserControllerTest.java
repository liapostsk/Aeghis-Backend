package com.tfg.aegis.controller;

import com.tfg.aegis.model.dto.UserDto;
import com.tfg.aegis.model.enums.UserEnums;
import com.tfg.aegis.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private UserDto userDto;
    private static final String CLERK_ID = "clerk_123";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(USER_ID);
        userDto.setClerkId(CLERK_ID);
        userDto.setEmail("test@example.com");

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(CLERK_ID);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_success() {
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getCurrentUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userDto, response.getBody());
        verify(userService).getUserByClerkId(CLERK_ID);
    }

    @Test
    void getUser_success() {
        when(userService.getUser(USER_ID)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUser(USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userDto, response.getBody());
        verify(userService).getUser(USER_ID);
    }

    @Test
    void getUser_withDifferentId_success() {
        Long differentId = 5L;
        UserDto differentUser = new UserDto();
        differentUser.setId(differentId);
        when(userService.getUser(differentId)).thenReturn(differentUser);

        ResponseEntity<UserDto> response = userController.getUser(differentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(differentUser, response.getBody());
        assertEquals(differentId, response.getBody().getId());
    }

    @Test
    void createUser_success() {
        UserDto newUser = new UserDto();
        newUser.setEmail("newuser@example.com");
        when(userService.createUser(any(UserDto.class))).thenReturn(USER_ID);

        ResponseEntity<Long> response = userController.createUser(newUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_ID, response.getBody());
        verify(userService).createUser(any(UserDto.class));
        assertEquals(CLERK_ID, newUser.getClerkId());
    }

    @Test
    void createUser_setsClerkIdFromContext() {
        UserDto newUser = new UserDto();
        newUser.setEmail("test@example.com");
        when(userService.createUser(any(UserDto.class))).thenReturn(2L);

        userController.createUser(newUser);

        assertEquals(CLERK_ID, newUser.getClerkId());
        verify(userService).createUser(newUser);
    }

    @Test
    void updateUser_success() {
        UserDto updatedUser = new UserDto();
        updatedUser.setEmail("updated@example.com");
        doNothing().when(userService).updateUser(eq(USER_ID), any(UserDto.class));

        ResponseEntity<Void> response = userController.updateUser(USER_ID, updatedUser);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).updateUser(USER_ID, updatedUser);
    }

    @Test
    void updateUser_withNullId_success() {
        UserDto updatedUser = new UserDto();
        doNothing().when(userService).updateUser(eq(null), any(UserDto.class));

        ResponseEntity<Void> response = userController.updateUser(null, updatedUser);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).updateUser(null, updatedUser);
    }

    @Test
    void deleteUser_success() {
        doNothing().when(userService).deleteUser(USER_ID);

        ResponseEntity<Void> response = userController.deleteUser(USER_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).deleteUser(USER_ID);
    }

    @Test
    void deleteUser_withNullId_success() {
        doNothing().when(userService).deleteUser(null);

        ResponseEntity<Void> response = userController.deleteUser(null);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(null);
    }

    @Test
    void userExistsByPhone_existingUser_returnsUserId() {
        String phone = "+1234567890";
        when(userService.userExistsByPhone(phone)).thenReturn(USER_ID);

        ResponseEntity<Long> response = userController.userExistsByPhone(phone);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(USER_ID, response.getBody());
        verify(userService).userExistsByPhone(phone);
    }

    @Test
    void userExistsByPhone_nonExistingUser_returnsNull() {
        String phone = "+9999999999";
        when(userService.userExistsByPhone(phone)).thenReturn(null);

        ResponseEntity<Long> response = userController.userExistsByPhone(phone);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).userExistsByPhone(phone);
    }

    @Test
    void userExistsByPhone_withDifferentPhoneFormats_success() {
        String[] phones = {"+34666777888", "666777888", "+1-555-123-4567"};

        for (String phone : phones) {
            when(userService.userExistsByPhone(phone)).thenReturn(USER_ID);

                ResponseEntity<Long> response = userController.userExistsByPhone(phone);

                assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(userService).userExistsByPhone(phone);
        }
    }

    @Test
    void addPhotoToUser_success() {
        String photoUrl = "https://example.com/photo.jpg";
        doNothing().when(userService).addPhotoToUser(USER_ID, photoUrl);

        ResponseEntity<Void> response = userController.addPhotoToUser(USER_ID, photoUrl);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).addPhotoToUser(USER_ID, photoUrl);
    }

    @Test
    void addPhotoToUser_withBase64Photo_success() {
        String base64Photo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA...";
        doNothing().when(userService).addPhotoToUser(USER_ID, base64Photo);

        ResponseEntity<Void> response = userController.addPhotoToUser(USER_ID, base64Photo);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).addPhotoToUser(USER_ID, base64Photo);
    }

    @Test
    void getUnverifiedUsers_withUsers_returnsList() {
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setVerify(UserEnums.VerificationStatus.PENDING);

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setVerify(UserEnums.VerificationStatus.PENDING);

        List<UserDto> unverifiedUsers = Arrays.asList(user1, user2);
        when(userService.getUnverifiedUsers()).thenReturn(unverifiedUsers);

        ResponseEntity<List<UserDto>> response = userController.getUnverifiedUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(userService).getUnverifiedUsers();
    }

    @Test
    void getUnverifiedUsers_noUsers_returnsEmptyList() {
        when(userService.getUnverifiedUsers()).thenReturn(Collections.emptyList());

        ResponseEntity<List<UserDto>> response = userController.getUnverifiedUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(userService).getUnverifiedUsers();
    }

    @Test
    void verifyUser_withVerifiedStatus_success() {
        String status = "VERIFIED";
        doNothing().when(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.VERIFIED);

        ResponseEntity<Void> response = userController.verifyUser(USER_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.VERIFIED);
    }

    @Test
    void verifyUser_withRejectedStatus_success() {
        String status = "REJECTED";
        doNothing().when(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.REJECTED);

        ResponseEntity<Void> response = userController.verifyUser(USER_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.REJECTED);
    }

    @Test
    void verifyUser_withPendingStatus_success() {
        String status = "PENDING";
        doNothing().when(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.PENDING);

        ResponseEntity<Void> response = userController.verifyUser(USER_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.PENDING);
    }

    @Test
    void verifyUser_withLowercaseStatus_success() {
        String status = "verified";
        doNothing().when(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.VERIFIED);

        ResponseEntity<Void> response = userController.verifyUser(USER_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.VERIFIED);
    }

    @Test
    void verifyUser_withMixedCaseStatus_success() {
        String status = "VeRiFiEd";
        doNothing().when(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.VERIFIED);

        ResponseEntity<Void> response = userController.verifyUser(USER_ID, status);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).verifyUser(USER_ID, UserEnums.VerificationStatus.VERIFIED);
    }
}
