package at.fhv.userservice.rest;


import at.fhv.userservice.application.services.CreateUserService;
import at.fhv.userservice.application.services.DeleteUserService;
import at.fhv.userservice.application.services.GetUserService;
import at.fhv.userservice.application.services.UpdateUserService;
import at.fhv.userservice.rest.dtos.CreateUserDTO;
import at.fhv.userservice.rest.dtos.GetUserDTO;
import at.fhv.userservice.rest.dtos.UpdateUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserRestController {
    private final GetUserService getUserService;
    private final CreateUserService createUserService;
    private final UpdateUserService updateUserService;
    private final DeleteUserService deleteUserService;

    public UserRestController(GetUserService getUserService, CreateUserService createUserService,
                              UpdateUserService updateUserService, DeleteUserService deleteUserService) {
        this.getUserService = getUserService;
        this.createUserService = createUserService;
        this.updateUserService = updateUserService;
        this.deleteUserService = deleteUserService;
    }

    @GetMapping
    public ResponseEntity<List<GetUserDTO>> getAllUsers() {
        return ResponseEntity.ok(getUserService.getUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserDTO> getUser(@PathVariable UUID id) {
        GetUserDTO user = getUserService.getUser(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @Operation(description = "Creates a new user. A new cart will be automatically created.")
    @PostMapping
    public ResponseEntity<GetUserDTO> createUser(@Valid @RequestBody CreateUserDTO user) {
        GetUserDTO createdUser = createUserService.createUser(user.getFirstName(), user.getLastName(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetUserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDTO userDTO) {
        GetUserDTO existingUser = getUserService.getUser(id);
        if (existingUser != null) {
            GetUserDTO updatedUser = updateUserService.updateUser(userDTO);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(description = "Sets the user status to INACTIVE. The user's cart status will also be set to INACTIVE.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        deleteUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
