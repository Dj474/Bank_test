package com.example.bankcards.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardDtoIn {

    @NotBlank(message = "Card number must not be blank")
    @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
    private String cardNumber;

    @NotBlank(message = "Card holder must not be blank")
    private String cardHolder;

    @NotNull(message = "Expiration date must not be null")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    private BigDecimal balance;

    @NotNull(message = "User ID must not be null")
    private Long userId;
}