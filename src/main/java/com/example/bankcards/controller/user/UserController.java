package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management Controller", description = "User administration operations (Available to ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user with any role (ADMIN/USER)")
    public UserDtoOut createUser(@Valid @RequestBody UserDtoIn dtoIn) {
        return userService.createUser(dtoIn);
    }

    @GetMapping
    @Operation(summary = "Get a list of all users (with pagination)")
    public Page<UserDtoOut> getAllUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed user information by ID")
    public UserDtoOut getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove the user from the system")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
