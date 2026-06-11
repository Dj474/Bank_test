package com.example.bankcards.repository.card;

import com.example.bankcards.entity.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
