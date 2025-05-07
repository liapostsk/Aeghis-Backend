package com.tfg.aegis.user;

import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = mock(ModelMapper.class);
        userController.mapper = mapper;
    }

    @Test
    void testGetUser() {
        Long userId = 1L;
        User user = new User();
        UserDto userDto = new UserDto();

        when(userService.getUser(userId)).thenReturn(user);
        when(mapper.map(user, UserDto.class)).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user_123456");

        ResponseEntity<Long> response = userController.createUser(userDto, jwt);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
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
