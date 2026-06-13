package com.example.bankcards.repository.card;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.util.enums.card.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserId(Long userId, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long id, Long userId);

    default Card byId(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Card not found"));
    }

    boolean existsByCardNumber(String cardNumber);

    @Modifying
    @Query("UPDATE Card c SET c.status = :targetStatus " +
            "WHERE c.status = :activeStatus AND c.expirationDate < :now")
    void updateStatusForExpiredCards(@Param("targetStatus") CardStatus targetStatus,
                                    @Param("activeStatus") CardStatus activeStatus,
                                    @Param("now") LocalDate now);

}
