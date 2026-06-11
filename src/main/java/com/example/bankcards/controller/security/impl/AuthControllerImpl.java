package com.example.bankcards.controller.security.impl;

import com.example.bankcards.controller.security.AuthController;
import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;
import com.example.bankcards.security.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Override
    public JwtDtoOut register(AuthenticationDtoIn userDtoIn) {
        return authService.register(userDtoIn);
    }

    @Override
    public JwtDtoOut login(AuthenticationDtoIn userDtoIn) {
        return authService.login(userDtoIn);
    }

    @Override
    public JwtRefreshDtoOut refreshToken(JwtRefreshDtoIn refreshDtoIn) {
        return authService.refreshToken(refreshDtoIn);
    }

}
