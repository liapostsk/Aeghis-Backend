package com.tfg.aegis.controller;

import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    private GroupDto groupDto;
    private static final Long GROUP_ID = 123L;
    private static final Long USER_ID = 7L;
    private static final String CODE = "INV-ABC";

    @BeforeEach
    void setUp() {
        groupDto = new GroupDto();
        groupDto.setId(GROUP_ID);
        groupDto.setName("Test Group");
    }

    @Test
    void createGroup_success() {
        
        GroupDto payload = new GroupDto();
        when(groupService.createGroup(payload)).thenReturn(GROUP_ID);

        ResponseEntity<Long> response = groupController.createGroup(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(GROUP_ID, response.getBody());
        verify(groupService, times(1)).createGroup(payload);
    }

    @Test
    void createGroup_withDifferentData_success() {
        
        GroupDto payload = new GroupDto();
        payload.setName("New Group");
        payload.setType(GroupEnums.TypeGroup.CONFIANZA);
        Long newId = 999L;
        when(groupService.createGroup(payload)).thenReturn(newId);

        ResponseEntity<Long> response = groupController.createGroup(payload);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newId, response.getBody());
        verify(groupService).createGroup(payload);
    }

    @Test
    void getGroupById_success() {
        
        when(groupService.getGroupById(GROUP_ID)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = groupController.getGroupById(GROUP_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupDto, response.getBody());
        assertEquals(GROUP_ID, response.getBody().getId());
        verify(groupService, times(1)).getGroupById(GROUP_ID);
    }

    @Test
    void getGroupById_withDifferentId_success() {
        
        Long differentId = 555L;
        GroupDto differentGroup = new GroupDto();
        differentGroup.setId(differentId);
        when(groupService.getGroupById(differentId)).thenReturn(differentGroup);

        ResponseEntity<GroupDto> response = groupController.getGroupById(differentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(differentId, response.getBody().getId());
        verify(groupService).getGroupById(differentId);
    }

    @Test
    void joinGroup_success() {
        
        when(groupService.joinGroup(USER_ID, CODE)).thenReturn(GROUP_ID);

        ResponseEntity<Long> response = groupController.joinGroup(USER_ID, CODE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(GROUP_ID, response.getBody());
        verify(groupService, times(1)).joinGroup(USER_ID, CODE);
    }

    @Test
    void joinGroup_withDifferentUserAndCode_success() {
        
        Long differentUserId = 42L;
        String differentCode = "DIFFERENT-CODE";
        Long resultGroupId = 777L;
        when(groupService.joinGroup(differentUserId, differentCode)).thenReturn(resultGroupId);

        ResponseEntity<Long> response = groupController.joinGroup(differentUserId, differentCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultGroupId, response.getBody());
        verify(groupService).joinGroup(differentUserId, differentCode);
    }

    @Test
    void getAllMyGroupsByType_confianza_success() {
        
        GroupEnums.TypeGroup type = GroupEnums.TypeGroup.CONFIANZA;
        GroupDto g1 = new GroupDto();
        g1.setId(1L);
        GroupDto g2 = new GroupDto();
        g2.setId(2L);
        List<GroupDto> expected = Arrays.asList(g1, g2);
        when(groupService.getAllMyGroupsByType(type)).thenReturn(expected);

        ResponseEntity<List<GroupDto>> response = groupController.getAllMyGroupsByType(type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(groupService, times(1)).getAllMyGroupsByType(type);
    }

    @Test
    void getAllMyGroupsByType_emptyList_success() {
        
        GroupEnums.TypeGroup type = GroupEnums.TypeGroup.CONFIANZA;
        when(groupService.getAllMyGroupsByType(type)).thenReturn(Collections.emptyList());

        ResponseEntity<List<GroupDto>> response = groupController.getAllMyGroupsByType(type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(groupService).getAllMyGroupsByType(type);
    }

    @Test
    void getAllMyGroups_success() {
        
        GroupDto group1 = new GroupDto();
        group1.setId(1L);
        GroupDto group2 = new GroupDto();
        group2.setId(2L);
        GroupDto group3 = new GroupDto();
        group3.setId(3L);
        List<GroupDto> allGroups = Arrays.asList(group1, group2, group3);
        when(groupService.getAllMyGroups()).thenReturn(allGroups);

        ResponseEntity<List<GroupDto>> response = groupController.getAllMyGroups();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(groupService).getAllMyGroups();
    }

    @Test
    void getAllMyGroups_emptyList_success() {
        
        when(groupService.getAllMyGroups()).thenReturn(Collections.emptyList());

        ResponseEntity<List<GroupDto>> response = groupController.getAllMyGroups();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(groupService).getAllMyGroups();
    }

    @Test
    void exitGroup_success() {
        
        when(groupService.exitGroup(GROUP_ID, USER_ID)).thenReturn(groupDto);

        ResponseEntity<GroupDto> response = groupController.exitGroup(GROUP_ID, USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupDto, response.getBody());
        verify(groupService, times(1)).exitGroup(GROUP_ID, USER_ID);
    }

    @Test
    void exitGroup_withDifferentIds_success() {
        
        Long groupId = 88L;
        Long userId = 99L;
        GroupDto result = new GroupDto();
        result.setId(groupId);
        when(groupService.exitGroup(groupId, userId)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.exitGroup(groupId, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupId, response.getBody().getId());
        verify(groupService).exitGroup(groupId, userId);
    }

    @Test
    void editGroup_success() {
        
        GroupDto incoming = new GroupDto();
        incoming.setName("Updated Name");
        GroupDto updated = new GroupDto();
        updated.setId(GROUP_ID);
        updated.setName("Updated Name");
        when(groupService.editGroup(GROUP_ID, incoming)).thenReturn(updated);

        ResponseEntity<GroupDto> response = groupController.editGroup(GROUP_ID, incoming);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        assertEquals("Updated Name", response.getBody().getName());
        verify(groupService, times(1)).editGroup(GROUP_ID, incoming);
    }

    @Test
    void editGroup_withDifferentData_success() {
        
        Long groupId = 100L;
        GroupDto incoming = new GroupDto();
        incoming.setDescription("New description");
        GroupDto updated = new GroupDto();
        updated.setId(groupId);
        when(groupService.editGroup(groupId, incoming)).thenReturn(updated);

        ResponseEntity<GroupDto> response = groupController.editGroup(groupId, incoming);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(groupService).editGroup(groupId, incoming);
    }

    @Test
    void deleteGroup_success() {
        
        doNothing().when(groupService).deleteGroup(GROUP_ID);

        ResponseEntity<Void> response = groupController.deleteGroup(GROUP_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(groupService, times(1)).deleteGroup(GROUP_ID);
    }

    @Test
    void deleteGroup_withDifferentId_success() {
        
        Long groupId = 999L;
        doNothing().when(groupService).deleteGroup(groupId);

        ResponseEntity<Void> response = groupController.deleteGroup(groupId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(groupService).deleteGroup(groupId);
    }

    @Test
    void addMember_success() {
        
        GroupDto result = new GroupDto();
        result.setId(GROUP_ID);
        when(groupService.addMember(GROUP_ID, USER_ID)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.addMember(GROUP_ID, USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(GROUP_ID, response.getBody().getId());
        verify(groupService).addMember(GROUP_ID, USER_ID);
    }

    @Test
    void addMember_withDifferentIds_success() {
        
        Long groupId = 50L;
        Long userId = 60L;
        GroupDto result = new GroupDto();
        result.setId(groupId);
        when(groupService.addMember(groupId, userId)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.addMember(groupId, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupId, response.getBody().getId());
        verify(groupService).addMember(groupId, userId);
    }

    @Test
    void removeMember_success() {
        
        doNothing().when(groupService).removeMember(GROUP_ID, USER_ID);

        ResponseEntity<Void> response = groupController.removeMember(GROUP_ID, USER_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(groupService).removeMember(GROUP_ID, USER_ID);
    }

    @Test
    void removeMember_withDifferentIds_success() {
        
        Long groupId = 200L;
        Long userId = 300L;
        doNothing().when(groupService).removeMember(groupId, userId);

        ResponseEntity<Void> response = groupController.removeMember(groupId, userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(groupService).removeMember(groupId, userId);
    }

    @Test
    void promoteToAdmin_success() {
        
        GroupDto result = new GroupDto();
        result.setId(GROUP_ID);
        when(groupService.promoteToAdmin(GROUP_ID, USER_ID)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.promoteToAdmin(GROUP_ID, USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(GROUP_ID, response.getBody().getId());
        verify(groupService).promoteToAdmin(GROUP_ID, USER_ID);
    }

    @Test
    void promoteToAdmin_withDifferentIds_success() {
        
        Long groupId = 11L;
        Long userId = 22L;
        GroupDto result = new GroupDto();
        result.setId(groupId);
        when(groupService.promoteToAdmin(groupId, userId)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.promoteToAdmin(groupId, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupId, response.getBody().getId());
        verify(groupService).promoteToAdmin(groupId, userId);
    }

    @Test
    void demoteAdmin_success() {
        
        GroupDto result = new GroupDto();
        result.setId(GROUP_ID);
        when(groupService.demoteAdmin(GROUP_ID, USER_ID)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.demoteAdmin(GROUP_ID, USER_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(GROUP_ID, response.getBody().getId());
        verify(groupService).demoteAdmin(GROUP_ID, USER_ID);
    }

    @Test
    void demoteAdmin_withDifferentIds_success() {
        
        Long groupId = 33L;
        Long userId = 44L;
        GroupDto result = new GroupDto();
        result.setId(groupId);
        when(groupService.demoteAdmin(groupId, userId)).thenReturn(result);

        ResponseEntity<GroupDto> response = groupController.demoteAdmin(groupId, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(groupId, response.getBody().getId());
        verify(groupService).demoteAdmin(groupId, userId);
    }

    @Test
    void addPhotoToGroup_success() {
        
        String photoUrl = "https://example.com/photo.jpg";
        doNothing().when(groupService).addPhotoToGroup(GROUP_ID, photoUrl);

        ResponseEntity<Void> response = groupController.addPhotoToGroup(GROUP_ID, photoUrl);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(groupService).addPhotoToGroup(GROUP_ID, photoUrl);
    }

    @Test
    void addPhotoToGroup_withBase64Photo_success() {
        
        String base64Photo = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA...";
        doNothing().when(groupService).addPhotoToGroup(GROUP_ID, base64Photo);

        ResponseEntity<Void> response = groupController.addPhotoToGroup(GROUP_ID, base64Photo);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(groupService).addPhotoToGroup(GROUP_ID, base64Photo);
    }

    @Test
    void addPhotoToGroup_withDifferentGroupId_success() {
        
        Long groupId = 888L;
        String photo = "photo-url";
        doNothing().when(groupService).addPhotoToGroup(groupId, photo);

        ResponseEntity<Void> response = groupController.addPhotoToGroup(groupId, photo);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(groupService).addPhotoToGroup(groupId, photo);
    }
}
