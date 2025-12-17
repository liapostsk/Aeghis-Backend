package com.tfg.aegis.controller;

import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGroup() {
        GroupDto payload = new GroupDto();
        Long generatedId = 123L;

        when(groupService.createGroup(payload)).thenReturn(generatedId);

        ResponseEntity<Long> response = groupController.createGroup(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(generatedId, response.getBody());
        verify(groupService, times(1)).createGroup(payload);
    }

    @Test
    void testGetGroupById() {
        Long groupId = 99L;
        GroupDto dto = new GroupDto();

        when(groupService.getGroupById(groupId)).thenReturn(dto);

        ResponseEntity<GroupDto> response = groupController.getGroupById(groupId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(groupService, times(1)).getGroupById(groupId);
    }

    @Test
    void testJoinGroup() {
        Long userId = 7L;
        String code = "INV-ABC";
        Long groupId = 1001L;

        when(groupService.joinGroup(userId, code)).thenReturn(groupId);

        ResponseEntity<Long> response = groupController.joinGroup(userId, code);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupId, response.getBody());
        verify(groupService, times(1)).joinGroup(userId, code);
    }

    @Test
    void testGetAllMyGroupsByType() {
        GroupEnums.TypeGroup type = GroupEnums.TypeGroup.CONFIANZA;
        GroupDto g1 = new GroupDto();
        GroupDto g2 = new GroupDto();
        List<GroupDto> expected = List.of(g1, g2);

        when(groupService.getAllMyGroupsByType(type)).thenReturn(expected);

        ResponseEntity<List<GroupDto>> response = groupController.getAllMyGroupsByType(type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(groupService, times(1)).getAllMyGroupsByType(type);
    }

    @Test
    void testExitGroup() {
        Long groupId = 55L;
        Long userId = 9L;
        GroupDto result = new GroupDto();

        when(groupService.exitGroup(groupId, userId)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.exitGroup(groupId, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());
        verify(groupService, times(1)).exitGroup(groupId, userId);
    }

    @Test
    void testEditGroup() {
        Long groupId = 77L;
        GroupDto incoming = new GroupDto();
        GroupDto updated = new GroupDto();

        when(groupService.editGroup(groupId, incoming)).thenReturn(updated);

        ResponseEntity<GroupDto> response = groupController.editGroup(groupId, incoming);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        verify(groupService, times(1)).editGroup(groupId, incoming);
    }

    @Test
    void testDeleteGroup() {
        Long groupId = 777L;

        // doNothing() por defecto, pero lo dejamos explícito
        doNothing().when(groupService).deleteGroup(groupId);

        ResponseEntity<Void> response = groupController.deleteGroup(groupId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(groupService, times(1)).deleteGroup(groupId);
    }
}
