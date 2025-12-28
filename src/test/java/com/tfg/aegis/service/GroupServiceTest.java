package com.tfg.aegis.service;

import com.tfg.aegis.common.exception.ConflictException;
import com.tfg.aegis.common.exception.NotFoundException;
import com.tfg.aegis.common.exception.UnauthorizedException;
import com.tfg.aegis.model.mapper.GroupMapper;
import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.entity.Group;
import com.tfg.aegis.model.dto.GroupDto;
import com.tfg.aegis.repository.GroupRepository;
import com.tfg.aegis.repository.InvitationRepository;
import com.tfg.aegis.repository.UserRepository;
import com.tfg.aegis.model.entity.User;
import com.tfg.aegis.model.dto.UserDto;
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
import static org.mockito.ArgumentMatchers.eq;

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
        dto.setType(GroupEnums.TypeGroup.CONFIANZA);

        User owner = new User();
        owner.setId(10L);

        Group entityToMap = new Group();
        entityToMap.setName("G");
        entityToMap.setDescription("D");
        entityToMap.setType(GroupEnums.TypeGroup.CONFIANZA);

        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(mapper.toEntity(dto)).thenReturn(entityToMap);

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
        assertEquals(GroupEnums.TypeGroup.CONFIANZA, g.getType());
        assertEquals(GroupEnums.GroupState.PENDIENTE, g.getState());
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

    @Test
    void createGroup_withAdditionalMembers_ok() {
        GroupDto dto = new GroupDto();
        dto.setOwnerId(10L);
        dto.setName("G");
        dto.setDescription("D");
        dto.setType(GroupEnums.TypeGroup.CONFIANZA);
        dto.setMembersIds(Set.of(10L, 20L, 30L)); // incluye owner y 2 miembros adicionales

        User owner = new User();
        owner.setId(10L);
        User member1 = new User();
        member1.setId(20L);
        User member2 = new User();
        member2.setId(30L);

        Group entityToMap = new Group();
        entityToMap.setName("G");
        entityToMap.setDescription("D");
        entityToMap.setType(GroupEnums.TypeGroup.CONFIANZA);

        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(20L)).thenReturn(Optional.of(member1));
        when(userRepository.findById(30L)).thenReturn(Optional.of(member2));
        when(mapper.toEntity(dto)).thenReturn(entityToMap);

        Group saved = new Group();
        saved.setId(123L);
        when(groupRepository.save(any(Group.class))).thenReturn(saved);

        Long id = service.createGroup(dto);

        assertEquals(123L, id);
        ArgumentCaptor<Group> cap = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(cap.capture());
        Group g = cap.getValue();
        assertEquals(3, g.getMembers().size());
        assertTrue(g.getMembers().contains(owner));
        assertTrue(g.getMembers().contains(member1));
        assertTrue(g.getMembers().contains(member2));
    }

    @Test
    void createGroup_memberNotFound_throws() {
        GroupDto dto = new GroupDto();
        dto.setOwnerId(10L);
        dto.setMembersIds(Set.of(10L, 999L)); // 999 no existe

        User owner = new User();
        owner.setId(10L);

        Group entityToMap = new Group();
        when(mapper.toEntity(dto)).thenReturn(entityToMap);

        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createGroup(dto));
        assertTrue(ex.getMessage().contains("User not found"));
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
        group.setState(GroupEnums.GroupState.PENDIENTE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(invitationService.validateInvitation("INV-ABC")).thenReturn(inviteDto);
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));

        Long result = service.joinGroup(userId, code);

        assertEquals(100L, result);
        assertTrue(group.getMembers().contains(user));
        assertEquals(GroupEnums.GroupState.ACTIVO, group.getState());
        verify(groupRepository).save(group);
    }

    @Test
    void joinGroup_ok_addsMember_whenMembersNull() {
        Long userId = 7L;
        User user = new User(); user.setId(userId);
        GroupDto inviteDto = new GroupDto(); inviteDto.setId(1L);
        Group group = new Group(); group.setId(1L); group.setMembers(null); group.setState(GroupEnums.GroupState.ACTIVO);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(invitationService.validateInvitation("ABC")).thenReturn(inviteDto);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        Long result = service.joinGroup(userId, "ABC");

        assertEquals(1L, result);
        assertNotNull(group.getMembers());
        assertTrue(group.getMembers().contains(user));
        assertEquals(GroupEnums.GroupState.ACTIVO, group.getState());
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
        GroupEnums.TypeGroup type = GroupEnums.TypeGroup.CONFIANZA;
        // mock SecurityContext principal (string clerkId)
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_123");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto();
        me.setId(77L);
        when(userService.getUserByClerkId("clerk_123")).thenReturn(me);

        Group g1 = new Group(); g1.setId(1L);
        Group g2 = new Group(); g2.setId(2L);
        when(groupRepository.findByTypeAndMemberNotExpired(eq(type), eq(77L), any())).thenReturn(List.of(g1, g2));

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

        assertThrows(IllegalArgumentException.class, () -> service.getAllMyGroupsByType(GroupEnums.TypeGroup.CONFIANZA));
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
        group.setState(GroupEnums.GroupState.ACTIVO);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(u));

        GroupDto mapped = new GroupDto(); mapped.setId(groupId);
        when(mapper.toDto(group)).thenReturn(mapped);

        GroupDto out = service.exitGroup(groupId, userId);

        // usuario eliminado
        assertTrue(group.getMembers().isEmpty());
        // grupo cerrado por quedarse vacío
        assertEquals(GroupEnums.GroupState.CERRADO, group.getState());
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
        group.setState(GroupEnums.GroupState.ACTIVO);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        GroupDto mapped = new GroupDto(); mapped.setId(groupId);
        when(mapper.toDto(group)).thenReturn(mapped);

        GroupDto out = service.exitGroup(groupId, adminId);

        assertFalse(group.getMembers().contains(admin));
        assertEquals(GroupEnums.GroupState.ACTIVO, group.getState());
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

    /* =========================
     * getAllMyGroups
     * ========================= */
    @Test
    void getAllMyGroups_ok_returnsAllGroupsForCurrentUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_abc");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto();
        me.setId(50L);
        when(userService.getUserByClerkId("clerk_abc")).thenReturn(me);

        Group g1 = new Group(); g1.setId(1L);
        Group g2 = new Group(); g2.setId(2L);
        when(groupRepository.findByMembers_Id(50L)).thenReturn(List.of(g1, g2));

        GroupDto d1 = new GroupDto(); d1.setId(1L);
        GroupDto d2 = new GroupDto(); d2.setId(2L);
        when(mapper.toDto(g1)).thenReturn(d1);
        when(mapper.toDto(g2)).thenReturn(d2);

        List<GroupDto> result = service.getAllMyGroups();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getAllMyGroups_userWithoutId_throws() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_x");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto(); // id null
        when(userService.getUserByClerkId("clerk_x")).thenReturn(me);

        assertThrows(IllegalArgumentException.class, () -> service.getAllMyGroups());
    }

    /* =========================
     * addMember
     * ========================= */
    @Test
    void addMember_ok_addsUserToGroup() {
        Long groupId = 1L, userId = 2L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>());
        group.setState(GroupEnums.GroupState.ACTIVO);

        User user = new User();
        user.setId(userId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Group saved = new Group();
        saved.setId(groupId);
        when(groupRepository.save(group)).thenReturn(saved);

        GroupDto dto = new GroupDto();
        dto.setId(groupId);
        when(mapper.toDto(saved)).thenReturn(dto);

        GroupDto result = service.addMember(groupId, userId);

        assertTrue(group.getMembers().contains(user));
        assertEquals(groupId, result.getId());
        verify(groupRepository).save(group);
    }

    @Test
    void addMember_alreadyMember_returnsGroupUnchanged() {
        Long groupId = 1L, userId = 2L;
        User user = new User();
        user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(user)));
        group.setState(GroupEnums.GroupState.ACTIVO);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        GroupDto dto = new GroupDto();
        dto.setId(groupId);
        when(mapper.toDto(group)).thenReturn(dto);

        GroupDto result = service.addMember(groupId, userId);

        assertEquals(groupId, result.getId());
        verify(groupRepository, never()).save(any());
    }

    @Test
    void addMember_groupClosed_throws() {
        Long groupId = 1L, userId = 2L;
        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>());
        group.setState(GroupEnums.GroupState.CERRADO);

        User user = new User();
        user.setId(userId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> service.addMember(groupId, userId));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void addMember_groupNotFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.addMember(1L, 2L));
    }

    @Test
    void addMember_userNotFound_throws() {
        Group g = new Group();
        g.setId(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(g));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.addMember(1L, 2L));
    }

    /* =========================
     * removeMember
     * ========================= */
    @Test
    void removeMember_ok_selfRemoval_removesUser() {
        Long groupId = 1L, userId = 2L;
        User user = new User(); user.setId(userId);
        User other = new User(); other.setId(3L);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(user, other)));
        group.setAdmins(new HashSet<>(Set.of(other)));
        group.setState(GroupEnums.GroupState.ACTIVO);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_self");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto();
        me.setId(userId);
        when(userService.getUserByClerkId("clerk_self")).thenReturn(me);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.removeMember(groupId, userId);

        assertFalse(group.getMembers().contains(user));
        verify(groupRepository).save(group);
    }

    @Test
    void removeMember_ok_adminRemovesOther() {
        Long groupId = 1L, adminId = 10L, victimId = 20L;
        User admin = new User(); admin.setId(adminId);
        User victim = new User(); victim.setId(victimId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(admin, victim)));
        group.setAdmins(new HashSet<>(Set.of(admin)));
        group.setState(GroupEnums.GroupState.ACTIVO);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_admin");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto();
        me.setId(adminId);
        when(userService.getUserByClerkId("clerk_admin")).thenReturn(me);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(victimId)).thenReturn(Optional.of(victim));

        service.removeMember(groupId, victimId);

        assertFalse(group.getMembers().contains(victim));
        verify(groupRepository).save(group);
    }

    @Test
    void removeMember_nonAdminTriesToRemoveOther_throws() {
        Long groupId = 1L, currentUserId = 5L, victimId = 6L;
        User current = new User(); current.setId(currentUserId);
        User victim = new User(); victim.setId(victimId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(current, victim)));
        group.setAdmins(new HashSet<>());
        group.setState(GroupEnums.GroupState.ACTIVO);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_current");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));

        UserDto me = new UserDto();
        me.setId(currentUserId);
        when(userService.getUserByClerkId("clerk_current")).thenReturn(me);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(victimId)).thenReturn(Optional.of(victim));

        assertThrows(UnauthorizedException.class, () -> service.removeMember(groupId, victimId));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void removeMember_userNotMember_throws() {
        Long groupId = 1L, userId = 9L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>());
        group.setAdmins(new HashSet<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> service.removeMember(groupId, userId));
    }

    @Test
    void removeMember_groupClosed_throws() {
        Long groupId = 1L, userId = 2L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(user)));
        group.setAdmins(new HashSet<>());
        group.setState(GroupEnums.GroupState.CERRADO);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () -> service.removeMember(groupId, userId));
    }

    @Test
    void removeMember_lastMember_temporalGroup_closesGroup() {
        Long groupId = 1L, userId = 2L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setType(GroupEnums.TypeGroup.TEMPORAL);
        group.setMembers(new HashSet<>(Set.of(user)));
        group.setAdmins(new HashSet<>(Set.of(user)));
        group.setState(GroupEnums.GroupState.ACTIVO);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_user");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
        UserDto me = new UserDto(); me.setId(userId);
        when(userService.getUserByClerkId("clerk_user")).thenReturn(me);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.removeMember(groupId, userId);

        assertEquals(GroupEnums.GroupState.CERRADO, group.getState());
        verify(groupRepository).save(group);
        verify(groupRepository, never()).delete(any());
    }

    @Test
    void removeMember_lastMember_nonTemporalGroup_deletesGroup() {
        Long groupId = 1L, userId = 2L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setType(GroupEnums.TypeGroup.CONFIANZA);
        group.setMembers(new HashSet<>(Set.of(user)));
        group.setAdmins(new HashSet<>(Set.of(user)));
        group.setState(GroupEnums.GroupState.ACTIVO);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_user");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
        UserDto me = new UserDto(); me.setId(userId);
        when(userService.getUserByClerkId("clerk_user")).thenReturn(me);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        service.removeMember(groupId, userId);

        verify(groupRepository).delete(group);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void removeMember_noAdminsLeft_assignsNewAdmin() {
        Long groupId = 1L, adminId = 10L, otherId = 20L;
        User admin = new User(); admin.setId(adminId);
        User other = new User(); other.setId(otherId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(admin, other)));
        group.setAdmins(new HashSet<>(Set.of(admin)));
        group.setState(GroupEnums.GroupState.ACTIVO);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("clerk_admin");
        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
        UserDto me = new UserDto(); me.setId(adminId);
        when(userService.getUserByClerkId("clerk_admin")).thenReturn(me);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

        service.removeMember(groupId, adminId);

        assertFalse(group.getMembers().contains(admin));
        assertTrue(group.getAdmins().contains(other));
        verify(groupRepository).save(group);
    }

    @Test
    void removeMember_groupNotFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.removeMember(1L, 2L));
    }

    @Test
    void removeMember_userNotFound_throws() {
        Group g = new Group(); g.setId(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(g));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.removeMember(1L, 2L));
    }

    /* =========================
     * promoteToAdmin
     * ========================= */
    @Test
    void promoteToAdmin_ok_promotesUser() {
        Long groupId = 1L, userId = 2L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>(Set.of(user)));
        group.setAdmins(new HashSet<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Group saved = new Group();
        saved.setId(groupId);
        when(groupRepository.save(group)).thenReturn(saved);

        GroupDto dto = new GroupDto();
        dto.setId(groupId);
        when(mapper.toDto(saved)).thenReturn(dto);

        GroupDto result = service.promoteToAdmin(groupId, userId);

        assertTrue(group.getAdmins().contains(user));
        assertEquals(groupId, result.getId());
        verify(groupRepository).save(group);
    }

    @Test
    void promoteToAdmin_userNotMember_throws() {
        Long groupId = 1L, userId = 9L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setMembers(new HashSet<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.promoteToAdmin(groupId, userId));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void promoteToAdmin_groupNotFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.promoteToAdmin(1L, 2L));
    }

    @Test
    void promoteToAdmin_userNotFound_throws() {
        Group g = new Group(); g.setId(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(g));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.promoteToAdmin(1L, 2L));
    }

    /* =========================
     * demoteAdmin
     * ========================= */
    @Test
    void demoteAdmin_ok_demotesUser() {
        Long groupId = 1L, userId = 2L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setAdmins(new HashSet<>(Set.of(user)));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Group saved = new Group();
        saved.setId(groupId);
        when(groupRepository.save(group)).thenReturn(saved);

        GroupDto dto = new GroupDto();
        dto.setId(groupId);
        when(mapper.toDto(saved)).thenReturn(dto);

        GroupDto result = service.demoteAdmin(groupId, userId);

        assertFalse(group.getAdmins().contains(user));
        assertEquals(groupId, result.getId());
        verify(groupRepository).save(group);
    }

    @Test
    void demoteAdmin_userNotAdmin_throws() {
        Long groupId = 1L, userId = 9L;
        User user = new User(); user.setId(userId);

        Group group = new Group();
        group.setId(groupId);
        group.setAdmins(new HashSet<>());

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> service.demoteAdmin(groupId, userId));
        verify(groupRepository, never()).save(any());
    }

    @Test
    void demoteAdmin_groupNotFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.demoteAdmin(1L, 2L));
    }

    @Test
    void demoteAdmin_userNotFound_throws() {
        Group g = new Group(); g.setId(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(g));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.demoteAdmin(1L, 2L));
    }

    /* =========================
     * addPhotoToGroup
     * ========================= */
    @Test
    void addPhotoToGroup_ok_updatesImageUrl() {
        Long groupId = 1L;
        String photo = "base64photo";

        Group group = new Group();
        group.setId(groupId);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        service.addPhotoToGroup(groupId, photo);

        assertEquals(photo, group.getImageUrl());
        verify(groupRepository).save(group);
    }

    @Test
    void addPhotoToGroup_groupNotFound_throws() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.addPhotoToGroup(1L, "photo"));
    }
}
