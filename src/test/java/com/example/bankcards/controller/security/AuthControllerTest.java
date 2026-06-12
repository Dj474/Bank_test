package com.example.bankcards.controller.security;

import com.example.bankcards.dto.security.AuthenticationDtoIn;
import com.example.bankcards.dto.security.JwtDtoOut;
import com.example.bankcards.dto.security.JwtRefreshDtoIn;
import com.example.bankcards.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private AuthenticationDtoIn registerDto;

    @BeforeEach
    void setUp() {
        registerDto = new AuthenticationDtoIn();
        registerDto.setUsername("real_integration_user");
        registerDto.setPassword("secure_password");
    }

    @Test
    void register_And_Then_Login_IntegrationFlow() throws Exception {
        String registerResponseJson = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        boolean userExistsInDb = userRepository.existsByUsername("real_integration_user");
        assertThat(userExistsInDb).isTrue();

        JwtDtoOut registerResult = objectMapper.readValue(registerResponseJson, JwtDtoOut.class);

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        JwtRefreshDtoIn refreshDtoIn = new JwtRefreshDtoIn();
        refreshDtoIn.setRefreshToken(registerResult.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshDtoIn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(registerResult.getRefreshToken()));
    }

    @Test
    void login_ReturnsNotFound_WhenUserDoesNotExistInDb() throws Exception {
        AuthenticationDtoIn nonExistentUser = new AuthenticationDtoIn();
        nonExistentUser.setUsername("ghost_user");
        nonExistentUser.setPassword("some_pass");

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andExpect(status().isNotFound());
    }
}