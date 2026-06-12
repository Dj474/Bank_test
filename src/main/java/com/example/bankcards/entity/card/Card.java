package com.example.bankcards.entity.card;

import com.example.bankcards.entity.user.User;
import com.example.bankcards.util.converter.card.CardNumberConverter;
import com.example.bankcards.util.enums.card.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false)
    @Convert(converter = CardNumberConverter.class)
    private String cardNumber;

    @Column(name = "card_holder", nullable = false, length = 100)
    private String cardHolder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getMaskedCardNumber() {
        if (this.cardNumber == null || this.cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        String lastFourDigits = this.cardNumber.substring(this.cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigits;
    }

}