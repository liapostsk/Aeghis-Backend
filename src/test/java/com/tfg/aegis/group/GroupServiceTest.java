package com.tfg.aegis.group;

import com.tfg.aegis.group.mapper.GroupMapper;
import com.tfg.aegis.group.model.Enums;
import com.tfg.aegis.group.model.Group;
import com.tfg.aegis.group.model.GroupDto;
import com.tfg.aegis.invitation.InvitationRepository;
import com.tfg.aegis.invitation.InvitationService;
import com.tfg.aegis.user.UserRepository;
import com.tfg.aegis.user.UserService;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvitationRepository invitationRepository;
    @Mock private UserService userService;
    @Mock private InvitationService invitationService;
    @Mock private GroupMapper mapper;

    @InjectMocks
    private GroupService service;

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    /* =========================
     * createGroup
     * ========================= */
    @Test
    void createGroup_ok_setsOwnerAndStateAndSaves() {
        GroupDto dto = new GroupDto();
        dto.setOwnerId(10L);
        dto.setName("G");
        dto.setDescription("D");
        dto.setType(Enums.TypeGroup.CONFIANZA);

        User owner = new User();
        owner.setId(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        Group saved = new Group();
        saved.setId(123L);
        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        Long id = service.createGroup(dto);

        assertEquals(123L, id);
        ArgumentCaptor<Group> cap = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(cap.capture());
        Group g = cap.getValue();
        assertEquals("G", g.getName());
        assertEquals("D", g.getDescription());
        assertEquals(owner, g.getOwner());
        assertTrue(g.getMembers().contains(owner));
        assertEquals(Enums.TypeGroup.CONFIANZA, g.getType());
        assertEquals(Enums.GroupState.PENDIENTE, g.getState());
    }

    @Test
    void createGroup_ownerNotFound_throws() {
        GroupDto dto = new GroupDto();
        dto.setOwnerId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createGroup(dto));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(groupRepository, never()).save(any());
    }

    /* =========================
     * getGroupById
     * ========================= */
    @Test
    void getGroupById_ok_mapsToDto() {
        Group group = new Group();
        group.setId(5L);
        GroupDto dto = new GroupDto();
        dto.setId(5L);

        when(groupRepository.findById(5L)).thenReturn(Optional.of(group));
        when(mapper.toDto(group)).thenReturn(dto);

        GroupDto out = service.getGroupById(5L);
        assertEquals(5L, out.getId());
    }

    @Test
    void getGroupById_notFound_throws() {
        when(groupRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getGroupById(404L));
    }

    /* =========================
     * joinGroup
     * ========================= */
    @Test
    void joinGroup_ok_addsMember_activatesIfPending() {
        Long userId = 7L;
        String code = " inv-abc "; // con espacios y minúsculas para validar normalización

        User user = new User();
        user.setId(userId);

        GroupDto inviteDto = new GroupDto();
        inviteDto.setId(100L);

        Group group = new Group();
        group.setId(100L);
        group.setMembers(new HashSet<>());
        group.setState(Enums.GroupState.PENDIENTE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(invitationService.validateInvitation("INV-ABC")).thenReturn(inviteDto);
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));

        Long result = service.joinGroup(userId, code);

        assertEquals(100L, result);
        assertTrue(group.getMembers().contains(user));
        assertEquals(Enums.GroupState.ACTIVO, group.getState());
        verify(groupRepository).save(group);
    }

    @Test
    void joinGroup_ok_addsMember_whenMembersNull() {
        Long userId = 7L;
        User user = new User(); user.setId(userId);
        GroupDto inviteDto = new GroupDto(); inviteDto.setId(1L);
        Group group = new Group(); group.setId(1L); group.setMembers(null); group.setState(Enums.GroupState.ACTIVO);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(invitationService.validateInvitation("ABC")).thenReturn(inviteDto);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        Long result = service.joinGroup(userId, "ABC");

        assertEquals(1L, result);
        assertNotNull(group.getMembers());
        assertTrue(group.getMembers().contains(user));
        assertEquals(Enums.GroupState.ACTIVO, group.getState());
        verify(groupRepository).save(group);
    }

    @Test
    void joinGroup_blankCode_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () -> service.joinGroup(1L, "   "));
    }

    @Test
    void joinGroup_userNotFound_throws() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.joinGroup(9L, "X"));
    }

    @Test
    void joinGroup_groupFromInvitationNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        GroupDto inviteDto = new GroupDto(); inviteDto.setId(999L);
        when(invitationService.validateInvitation("CODE")).thenReturn(inviteDto);
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.joinGroup(1L, "CODE"));
    }

    /* =========================
     * getAllMyGroupsByType
     * ========================= */
    @Test
    void getAllMyGroupsByType_ok_usesSecurityContextAndMaps() {
        Enums.TypeGroup type = Enums.TypeGroup.CONFIANZA;
        // mock SecurityContext principal (string clerkId)
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_123");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto();
        me.setId(77L);
        when(userService.getUserByClerkId("clerk_123")).thenReturn(me);

        Group g1 = new Group(); g1.setId(1L);
        Group g2 = new Group(); g2.setId(2L);
        when(groupRepository.findByTypeAndMembers_Id(type, 77L)).thenReturn(List.of(g1, g2));

        GroupDto d1 = new GroupDto(); d1.setId(1L);
        GroupDto d2 = new GroupDto(); d2.setId(2L);
        when(mapper.toDto(g1)).thenReturn(d1);
        when(mapper.toDto(g2)).thenReturn(d2);

        List<GroupDto> out = service.getAllMyGroupsByType(type);

        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getId());
        assertEquals(2L, out.get(1).getId());
    }

    @Test
    void getAllMyGroupsByType_nullType_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getAllMyGroupsByType(null));
    }

    @Test
    void getAllMyGroupsByType_userWithoutId_throws() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_X");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto(); // id null
        when(userService.getUserByClerkId("clerk_X")).thenReturn(me);

        assertThrows(IllegalArgumentException.class, () -> service.getAllMyGroupsByType(Enums.TypeGroup.CONFIANZA));
    }

    /* =========================
     * exitGroup
     * ========================= */
    @Test
    void exitGroup_ok_memberLeaves_nonAdmin_simpleRemoval_andCloseIfEmpty() {
        Long groupId = 1L; Long userId = 2L;

        User u = new User(); u.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(u)));
        group.setAdmins(new HashSet<>()); // no admin
        group.setState(Enums.GroupState.ACTIVO);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));

        GroupDto mapped = new GroupDto(); mapped.setId(groupId);
        when(mapper.toDto(group)).thenReturn(mapped);

        GroupDto out = service.exitGroup(groupId, userId);

        // usuario eliminado
        assertTrue(group.getMembers().isEmpty());
        // grupo cerrado por quedarse vacío
        assertEquals(Enums.GroupState.CERRADO, group.getState());
        verify(groupRepository).save(group);
        assertEquals(groupId, out.getId());
    }

    @Test
    void exitGroup_ok_adminLeaves_assignsNewAdminIfEmptyAdmins() {
        Long groupId = 1L; Long adminId = 10L;

        User admin = new User(); admin.setId(adminId);
        User another = new User(); another.setId(20L);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(admin, another)));
        group.setAdmins(new HashSet<>(Set.of(admin)));
        group.setState(Enums.GroupState.ACTIVO);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        GroupDto mapped = new GroupDto(); mapped.setId(groupId);
        when(mapper.toDto(group)).thenReturn(mapped);

        GroupDto out = service.exitGroup(groupId, adminId);

        assertFalse(group.getMembers().contains(admin));
        assertEquals(Enums.GroupState.ACTIVO, group.getState());
        verify(groupRepository).save(group);
        assertEquals(groupId, out.getId());
    }

    @Test
    void exitGroup_userNotMember_throws() {
        Long groupId = 1L; Long userId = 9L;
        User u = new User(); u.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>()); // vacío
        group.setAdmins(new HashSet<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.exitGroup(groupId, userId));
        assertTrue(ex.getMessage().contains("not a member"));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void exitGroup_groupNotFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.exitGroup(1L, 2L));
    }

    @Test
    void exitGroup_userNotFound_throws() {
        Group g = new Group(); g.setId(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(g));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.exitGroup(1L, 2L));
    }

    /* =========================
     * editGroup
     * ========================= */
    @Test
    void editGroup_ok_updatesFields_andReturnsDto() {
        Long groupId = 5L;
        GroupDto incoming = new GroupDto();
        incoming.setName("Nuevo");
        incoming.setDescription("Desc");

        Group entity = new Group();
        entity.setId(groupId);
        entity.setName("Viejo");
        entity.setDescription("Old");

        Group saved = new Group();
        saved.setId(groupId);
        saved.setName("Nuevo");
        saved.setDescription("Desc");

        GroupDto mapped = new GroupDto();
        mapped.setId(groupId);
        mapped.setName("Nuevo");
        mapped.setDescription("Desc");

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(entity));
        when(groupRepository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(mapped);

        GroupDto out = service.editGroup(groupId, incoming);

        assertEquals("Nuevo", out.getName());
        assertEquals("Desc", out.getDescription());
        verify(groupRepository).save(entity);
    }

    @Test
    void editGroup_notFound_throws() {
        when(groupRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.editGroup(9L, new GroupDto()));
    }

    /* =========================
     * deleteGroup
     * ========================= */
    @Test
    void deleteGroup_ok_whenZeroOrOneMember_clearsAdmins_deletesInvitations_andDeletesGroup() {
        Long groupId = 33L;

        Group group = new Group();
        group.setId(groupId);
        // 1 miembro -> permitido
        User only = new User(); only.setId(1L);
        group.setMembers(new HashSet<>(Set.of(only)));
        group.setAdmins(new HashSet<>(Set.of(only)));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        service.deleteGroup(groupId);

        assertTrue(group.getAdmins().isEmpty(), "admins deben limpiarse antes de borrar");
        verify(invitationRepository).deleteByGroupId(groupId);
        verify(groupRepository).delete(group);
    }

    @Test
    void deleteGroup_moreThanOneMember_throws() {
        Long groupId = 44L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(new User(), new User())));
        group.setAdmins(new HashSet<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.deleteGroup(groupId));
        assertTrue(ex.getMessage().contains("cannot be deleted"));
        verify(invitationRepository, never()).deleteByGroupId(anyLong());
        verify(groupRepository, never()).delete(any());
    }

    @Test
    void deleteGroup_notFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteGroup(1L));
    }
}
