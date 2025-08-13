package com.tfg.aegis.group;

import com.tfg.aegis.group.model.GroupDto;
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

    @PostMapping("/{groupId}/join")
    @Operation(summary = "Join a group", description = "Allows a user to join an existing group by its ID")
    public ResponseEntity<Void> joinGroup(@PathVariable Long groupId, @RequestParam Long userId, @RequestParam String code) {
        groupService.joinGroup(groupId, userId, code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/exit")
    @Operation(summary = "Exit a group", description = "Allows a user to exit an existing group by its ID")
    public ResponseEntity<Void> exitGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        groupService.exitGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

}
