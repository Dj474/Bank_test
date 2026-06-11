package com.example.bankcards.security.service;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;
import com.example.bankcards.entity.user.User;

public interface AuthService {

    JwtDtoOut register(AuthenticationDtoIn userDtoIn);

    JwtDtoOut login(AuthenticationDtoIn userDtoIn);

    JwtRefreshDtoOut refreshToken(JwtRefreshDtoIn refreshDtoIn);

    User getCurrentUser();
}
