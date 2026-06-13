package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.util.mapper.user.UserMapper;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.user.Role;
import com.example.bankcards.exception.BadRequestException; // Напиши сюда свой правильный импорт исключения
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDtoIn dtoIn;
    private User user;
    private UserDtoOut dtoOut;

    @BeforeEach
    void setUp() {
        dtoIn = new UserDtoIn();
        dtoIn.setUsername("test_user");
        dtoIn.setPassword("password");
        dtoIn.setRole("USER");

        user = User.builder()
                .id(1L)
                .username("test_user")
                .password("encoded_password")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        dtoOut = new UserDtoOut();
        dtoOut.setId(1L);
        dtoOut.setUsername("test_user");
        dtoOut.setRole("USER");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername(dtoIn.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(dtoIn.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDtoOut(user)).thenReturn(dtoOut);

        UserDtoOut result = userService.createUser(dtoIn);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test_user");
        assertThat(result.getRole()).isEqualTo("USER");

        verify(userRepository, times(1)).existsByUsername(dtoIn.getUsername());
        verify(passwordEncoder, times(1)).encode(dtoIn.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDtoOut(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenUsernameExists() {
        when(userRepository.existsByUsername(dtoIn.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dtoIn))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenRoleIsInvalid() {
        dtoIn.setRole("INVALID_ROLE");
        when(userRepository.existsByUsername(dtoIn.getUsername())).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(dtoIn))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid role. Use USER or ADMIN");

        verify(userRepository, never()).save(any(User.class));
    }
}