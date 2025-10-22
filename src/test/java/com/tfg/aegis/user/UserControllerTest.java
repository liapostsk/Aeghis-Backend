package com.tfg.aegis.user;

import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock private UserService userService;
    @Mock private ModelMapper mapper;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_123"); // <- String clerkId

        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto();
        when(userService.createUser(any(UserDto.class))).thenReturn(1L); // si tu controller llama al service

        ResponseEntity<Long> response = userController.createUser(userDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void testGetUser() {
        Long userId = 1L;
        User user = new User();
        UserDto userDto = new UserDto();

        when(mapper.map(user, UserDto.class)).thenReturn(userDto);
        when(userService.getUser(userId)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    void testUpdateUser() {
        Long userId = 1L;
        UserDto userDto = new UserDto();

        ResponseEntity<Void> response = userController.updateUser(userId, userDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
