package com.example.bankcards.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRefreshDtoIn {

    @NotBlank(message = "Refresh token must not be blank")
    private String refreshToken;

}
