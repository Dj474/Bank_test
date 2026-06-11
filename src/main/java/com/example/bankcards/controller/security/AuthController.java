package com.example.bankcards.controller.security;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/v1/auth")
public interface AuthController {

    @PostMapping("/register")
    JwtDtoOut register(@Valid @RequestBody AuthenticationDtoIn userDtoIn);

    @PostMapping("/signin")
    JwtDtoOut login(@Valid @RequestBody AuthenticationDtoIn userDtoIn);

    @PostMapping("/refresh")
    JwtRefreshDtoOut refreshToken(@Valid @RequestBody JwtRefreshDtoIn refreshDtoIn);

}
