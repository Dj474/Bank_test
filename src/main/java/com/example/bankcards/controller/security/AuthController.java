package com.example.bankcards.controller.security;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;
import com.example.bankcards.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for registration, authorization, and JWT token renewal")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "New user registration", description = "Creates a new user account in the system and returns the initial pair of JWT tokens")
    public JwtDtoOut register(@Valid @RequestBody AuthenticationDtoIn userDtoIn) {
        return authService.register(userDtoIn);
    }

    @PostMapping("/signin")
    @Operation(summary = "User Authorization (Log In)", description = "Verifies user credentials and issues an active pair of access and refresh tokens")
    public JwtDtoOut login(@Valid @RequestBody AuthenticationDtoIn userDtoIn) {
        return authService.login(userDtoIn);
    }

    @PostMapping("/refresh")
    @Operation(summary = "JWT token renewal", description = "Accepts a valid (non-expired) refresh token and returns a new access token")
    public JwtRefreshDtoOut refreshToken(@Valid @RequestBody JwtRefreshDtoIn refreshDtoIn) {
        return authService.refreshToken(refreshDtoIn);
    }

}
