package com.example.bankcards.dto.card;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CardDtoOut {
    private Long id;
    private String cardNumber;
    private String cardHolder;
    private LocalDate expirationDate;
    private String status;
    private BigDecimal balance;
    private Long userId;
    private LocalDateTime createdAt;
}
