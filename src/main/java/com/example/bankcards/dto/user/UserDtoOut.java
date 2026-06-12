package com.example.bankcards.dto.user;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserDtoOut {

    private Long id;

    private String username;

    private String role;

    private LocalDateTime createdAt;
}
