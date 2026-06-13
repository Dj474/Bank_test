package com.example.bankcards.service.card;

import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.util.enums.card.CardStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class CardExpirationScheduler {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndExpireCards() {

        cardRepository.updateStatusForExpiredCards(
                CardStatus.EXPIRED,
                CardStatus.ACTIVE,
                LocalDate.now()
        );

    }
}
