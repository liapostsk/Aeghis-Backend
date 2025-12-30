package com.tfg.aegis.controller;

import com.tfg.aegis.service.GroupService;
import com.tfg.aegis.model.enums.GroupEnums;
import com.tfg.aegis.model.dto.GroupDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group", description = "API of groups")
@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "Create a new group", description = "Creates a new group with the provided details")
    @PostMapping
    public ResponseEntity<Long> createGroup(@RequestBody GroupDto groupDto) {
        Long groupId = groupService.createGroup(groupDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(groupId);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group by ID", description = "Retrieves a group by its ID")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long groupId) {
        GroupDto group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    @PostMapping("/join")
    @Operation(summary = "Join a group", description = "Allows a user to join an existing group by its ID")
    public ResponseEntity<Long> joinGroup(@RequestParam Long userId, @RequestParam String code) {
        Long groupId = groupService.joinGroup(userId, code);
        return ResponseEntity.ok(groupId);
    }

    @GetMapping("/{type}/my-groups")
    @Operation(summary = "Get all groups of the specific type", description = "Retrieves all groups of a specific type")
    public ResponseEntity<List<GroupDto>> getAllMyGroupsByType(@PathVariable(name = "type") GroupEnums.TypeGroup type) {
        List<GroupDto> groups = groupService.getAllMyGroupsByType(type);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/my-groups")
    @Operation(summary = "Get all groups of the user", description = "Retrieves all groups")
    public ResponseEntity<List<GroupDto>> getAllMyGroups() {
        List<GroupDto> groups = groupService.getAllMyGroups();
        return ResponseEntity.ok(groups);
    }

    @DeleteMapping("/{groupId}/exit")
    @Operation(summary = "Exit a group", description = "Allows a user to exit an existing group by its ID")
    public ResponseEntity<GroupDto> exitGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        GroupDto groupDto = groupService.exitGroup(groupId, userId);
        return ResponseEntity.ok(groupDto);
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Edit a group", description = "Edit an existing group by its ID")
    public ResponseEntity<GroupDto> editGroup(@PathVariable Long groupId, @RequestBody GroupDto groupDto) {
        GroupDto updatedGroup = groupService.editGroup(groupId, groupDto);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete a group", description = "Deletes an existing group by its ID")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/add-member/{userId}")
    @Operation(summary = "Add member", description = "Adds a user as member of the group (idempotent)")
    public ResponseEntity<GroupDto> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        GroupDto dto = groupService.addMember(groupId, userId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{groupId}/remove-member/{userId}")
    @Operation(summary = "Remove member", description = "Removes a user from the group")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/promote-admin/{userId}")
    @Operation(summary = "Promote to admin", description = "Promotes a member to admin (idempotent)")
    public ResponseEntity<GroupDto> promoteToAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        GroupDto dto = groupService.promoteToAdmin(groupId, userId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{groupId}/demote-admin/{userId}")
    @Operation(summary = "Demote admin", description = "Removes admin role from a user")
    public ResponseEntity<GroupDto> demoteAdmin(@PathVariable Long groupId, @PathVariable Long userId) {
        GroupDto dto = groupService.demoteAdmin(groupId, userId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Add image to group", description = "Adds an image to a group")
    @PostMapping("/{groupId}/photo")
    public ResponseEntity<Void> addPhotoToGroup(@PathVariable Long groupId, @RequestBody String photo) {
        groupService.addPhotoToGroup(groupId, photo);
        return ResponseEntity.noContent().build();
    }

}
