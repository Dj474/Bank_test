package com.example.bankcards.controller.security;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;
import com.example.bankcards.security.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public JwtDtoOut register(AuthenticationDtoIn userDtoIn) {
        return authService.register(userDtoIn);
    }

    @PostMapping("/signin")
    public JwtDtoOut login(AuthenticationDtoIn userDtoIn) {
        return authService.login(userDtoIn);
    }

    @PostMapping("/refresh")
    public JwtRefreshDtoOut refreshToken(JwtRefreshDtoIn refreshDtoIn) {
        return authService.refreshToken(refreshDtoIn);
    }

}
