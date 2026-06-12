package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapper.user.UserMapper;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserDtoOut> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDtoOut);
    }

    @Transactional(readOnly = true)
    public UserDtoOut getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toDtoOut(user);
    }

    @Transactional
    public UserDtoOut createUser(UserDtoIn dtoIn) {
        if (userRepository.existsByUsername(dtoIn.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(dtoIn.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Use USER or ADMIN");
        }

        User user = User.builder()
                .username(dtoIn.getUsername())
                .password(passwordEncoder.encode(dtoIn.getPassword()))
                .role(userRole)
                .createdAt(LocalDateTime.now())
                .build();

        return userMapper.toDtoOut(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}