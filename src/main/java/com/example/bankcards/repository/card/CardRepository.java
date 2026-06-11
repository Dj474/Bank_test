package com.example.bankcards.repository.card;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserId(Long userId, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long id, Long userId);

    default Card byId(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Card not found"));
    }


}
