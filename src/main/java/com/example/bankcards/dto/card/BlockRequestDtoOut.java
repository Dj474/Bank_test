package com.example.bankcards.dto.card;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class BlockRequestDtoOut {
    private Long id;
    private Long cardId;
    private String maskedCardNumber;
    private Long userId;
    private String username;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}