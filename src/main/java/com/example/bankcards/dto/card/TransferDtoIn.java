package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TransferDtoIn {

    @NotNull(message = "Source card ID must not be null")
    private Long fromCardId;

    @NotNull(message = "Destination card ID must not be null")
    private Long toCardId;

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;
}
