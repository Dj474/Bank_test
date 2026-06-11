package com.example.bankcards.security.service.impl;

import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.security.service.AuthService;
import com.example.bankcards.util.enums.user.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;

import java.awt.*;
import java.time.LocalDateTime;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private JwtDtoOut getJwtForUser(User user) {
        JwtDtoOut jwtDtoOut = new JwtDtoOut();
        jwtDtoOut.setAccessToken(jwtTokenProvider.generateAccessTokenFromId(user.getId(), user.getRole().name()));
        jwtDtoOut.setRefreshToken(jwtTokenProvider.generateRefreshTokenFromId(user.getId()));
        jwtDtoOut.setId(user.getId());
        return jwtDtoOut;
    }

    @Transactional
    public JwtDtoOut register(AuthenticationDtoIn userDtoIn) {
        if (userRepository.existsByUsername(userDtoIn.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        User user = User.builder()
                .username(userDtoIn.getUsername())
                .password(passwordEncoder.encode(userDtoIn.getPassword()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return getJwtForUser(user);
    }

    @Transactional(readOnly = true)
    public JwtDtoOut login(AuthenticationDtoIn userDtoIn) {
        User user = userRepository.findByUsername(userDtoIn.getUsername())
                .orElseThrow(() -> new NotFoundException("User not exists"));

        if (!passwordEncoder.matches(userDtoIn.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Wrong password");
        }

        return getJwtForUser(user);
    }

    @Transactional(readOnly = true)
    public JwtRefreshDtoOut refreshToken(JwtRefreshDtoIn refreshDtoIn) {
        if (!jwtTokenProvider.validateRefreshToken(refreshDtoIn.getRefreshToken())) {
            throw new ForbiddenException("Refresh token expired or invalid");
        }

        Long id = jwtTokenProvider.getUserIdFromRefreshToken(refreshDtoIn.getRefreshToken());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not exists"));

        JwtRefreshDtoOut jwtRefreshDtoOut = new JwtRefreshDtoOut();
        jwtRefreshDtoOut.setRefreshToken(refreshDtoIn.getRefreshToken());
        jwtRefreshDtoOut.setAccessToken(jwtTokenProvider.generateAccessTokenFromId(user.getId(), user.getRole().name()));

        return jwtRefreshDtoOut;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Long) {
            return userRepository.getReferenceById((Long) principal);
        }
        throw new ForbiddenException("Invalid credentials");
    }
}