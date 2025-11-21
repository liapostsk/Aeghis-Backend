package com.tfg.aegis.person.user;

import com.tfg.aegis.person.user.model.Enums;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import com.tfg.aegis.person.user.model.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "User", description = "API of Users")
@RequestMapping(value = "/user")
@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Operation(summary = "Get current user", description = "Returns the currently authenticated user based on JWT")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDto userDto = userService.getUserByClerkId(clerkId);

        log.info("Current user: {}", userDto);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Get", description = "Method that gets a User")
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable(name = "id") Long id) {
        UserDto userDto = userService.getUser(id);
        log.info("Current user: {}", userDto);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Create", description = "Method that creates a User")
    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody UserDto userDto) {
        log.info("UserDto: {}", userDto);
        String clerkId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userDto.setClerkId(clerkId);
        Long id = userService.createUser(userDto);
        log.info("Current user id: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "Update", description = "Method that update the info of a User")
    @PutMapping(path = "/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable(name = "id", required = false) Long id,  @RequestBody UserDto userDto) {
        userService.updateUser(id, userDto);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Delete", description = "Method that deletes a User")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id", required = false) Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Check Existence", description = "Method that checks if a User exists by phone number")
    @GetMapping(path = "/exists/{phone}")
    public ResponseEntity<Long> userExistsByPhone(@PathVariable(name = "phone") String phone) {
        Long userId = userService.userExistsByPhone(phone);
        log.info("Exists user with id: {}", userId);
        return ResponseEntity.ok(userId);
    }

    @Operation(summary = "Add photo", description = "Method that adds a photo to a User")
    @PostMapping(path = "/{id}/photo")
    public ResponseEntity<Void> addPhotoToUser(@PathVariable(name = "id") Long id, @RequestBody String photo) {
        userService.addPhotoToUser(id, photo);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get unverifyed users", description = "Method that gets the unverified users")
    @GetMapping(path = "/unverified")
    public ResponseEntity<List<UserDto>> getUnverifiedUsers() {
        List<UserDto> unverifiedUsers = userService.getUnverifiedUsers();
        return ResponseEntity.ok(unverifiedUsers);
    }

    @Operation(summary = "Verify user", description = "Method that verifies a user")
    @PostMapping(path = "/{id}/verify")
    public ResponseEntity<Void> verifyUser(@PathVariable(name = "id") Long id, @RequestParam(name = "verified") String status) {

        Enums.VerificationStatus verificationStatus = Enums.VerificationStatus.valueOf(status.toUpperCase());

        userService.verifyUser(id, verificationStatus);
        return ResponseEntity.noContent().build();
    }

}
