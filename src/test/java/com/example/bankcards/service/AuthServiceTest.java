package com.example.bankcards.service;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.dto.security.JwtRefreshDtoOut;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ForbiddenException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.security.jwt.JwtTokenProvider;
import com.example.bankcards.security.service.AuthService;
import com.example.bankcards.util.enums.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private AuthenticationDtoIn authDtoIn;
    private User user;

    @BeforeEach
    void setUp() {
        authDtoIn = new AuthenticationDtoIn();
        authDtoIn.setUsername("auth_user");
        authDtoIn.setPassword("raw_password");

        user = User.builder()
                .id(100L)
                .username("auth_user")
                .password("encoded_password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(authDtoIn.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(authDtoIn.getPassword())).thenReturn("encoded_password");

        when(jwtTokenProvider.generateAccessTokenFromId(any(), eq("USER"))).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshTokenFromId(any())).thenReturn("refresh_token");

        JwtDtoOut result = authService.register(authDtoIn);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access_token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh_token");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ThrowsBadRequest_WhenUserExists() {
        when(userRepository.existsByUsername(authDtoIn.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(authDtoIn))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername(authDtoIn.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authDtoIn.getPassword(), user.getPassword())).thenReturn(true);

        when(jwtTokenProvider.generateAccessTokenFromId(100L, "USER")).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshTokenFromId(100L)).thenReturn("refresh_token");

        JwtDtoOut result = authService.login(authDtoIn);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access_token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    void login_ThrowsNotFound_WhenUserNotExists() {
        when(userRepository.findByUsername(authDtoIn.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(authDtoIn))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not exists");
    }

    @Test
    void login_ThrowsUnauthorized_WhenPasswordWrong() {
        when(userRepository.findByUsername(authDtoIn.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authDtoIn.getPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(authDtoIn))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Wrong password");
    }

    @Test
    void refreshToken_Success() {
        JwtRefreshDtoIn refreshDtoIn = new JwtRefreshDtoIn();
        refreshDtoIn.setRefreshToken("valid_refresh_token");

        when(jwtTokenProvider.validateRefreshToken("valid_refresh_token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromRefreshToken("valid_refresh_token")).thenReturn(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessTokenFromId(100L, "USER")).thenReturn("new_access_token");

        JwtRefreshDtoOut result = authService.refreshToken(refreshDtoIn);

        assertThat(result).isNotNull();
        assertThat(result.getRefreshToken()).isEqualTo("valid_refresh_token");
        assertThat(result.getAccessToken()).isEqualTo("new_access_token");
    }

    @Test
    void refreshToken_ThrowsForbidden_WhenTokenInvalid() {
        JwtRefreshDtoIn refreshDtoIn = new JwtRefreshDtoIn();
        refreshDtoIn.setRefreshToken("invalid_token");

        when(jwtTokenProvider.validateRefreshToken("invalid_token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshDtoIn))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Refresh token expired or invalid");
    }

    @Test
    void getCurrentUser_Success() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(100L);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(userRepository.getReferenceById(100L)).thenReturn(user);

        User result = authService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUsername()).isEqualTo("auth_user");

        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_ThrowsForbidden_WhenPrincipalNotLong() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Invalid credentials");

        SecurityContextHolder.clearContext();
    }
}
