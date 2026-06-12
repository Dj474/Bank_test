package com.example.bankcards.controller.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .username("existing_worker")
                .password("raw_or_encoded_pass")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        existingUser = userRepository.save(existingUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_AsAdmin_Success() throws Exception {
        UserDtoIn dtoIn = new UserDtoIn();
        dtoIn.setUsername("new_unique_user");
        dtoIn.setPassword("pass123");
        dtoIn.setRole("USER");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoIn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("new_unique_user"));

        assertThat(userRepository.existsByUsername("new_unique_user")).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", existingUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("existing_worker"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", existingUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(existingUser.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "USER")
    void anyRequest_AsUser_ReturnsForbidden() throws Exception {
        UserDtoIn dtoIn = new UserDtoIn();
        dtoIn.setUsername("unauthorized_add");
        dtoIn.setPassword("password");
        dtoIn.setRole("USER");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoIn)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void anyRequest_AsAnonymous_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }
}