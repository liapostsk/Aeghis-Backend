package com.tfg.aegis.user;

import com.tfg.aegis.exception.user.UserNotFoundException;
import com.tfg.aegis.user.model.User;
import com.tfg.aegis.user.model.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "API of Users")
@RequestMapping(value = "/user")
@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    ModelMapper mapper;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get", description = "Method that gets a User")
    @GetMapping(path = "/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable(name = "id") Long id) {
        User user = this.userService.getUser(id);
        UserDto userDto = mapper.map(user, UserDto.class); //de user a userDto
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Create", description = "Method that creates a User")
    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody UserDto userDto) {
        Long id = this.userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "Update", description = "Method that update the info of a User")
    @PutMapping(path = "/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable(name = "id", required = false) Long id,  @RequestBody UserDto userDto) {
        this.userService.updateUser(id, userDto);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    @Operation(summary = "Delete", description = "Method that deletes a User")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id", required = false) Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

}
