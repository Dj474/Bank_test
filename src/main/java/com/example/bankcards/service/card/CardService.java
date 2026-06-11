package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.card.TransferDtoIn;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.card.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    private CardDtoOut mapToDtoOut(Card card) {
        CardDtoOut dto = new CardDtoOut();
        dto.setId(card.getId());
        dto.setCardNumber(card.getMaskedCardNumber());
        dto.setCardHolder(card.getCardHolder());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus().name());
        dto.setBalance(card.getBalance());
        dto.setUserId(card.getUser().getId());
        dto.setCreatedAt(card.getCreatedAt());
        return dto;
    }

    @Transactional
    public CardDtoOut createCard(CardDtoIn dtoIn) {
        User user = userRepository.byId(dtoIn.getUserId());

        Card card = Card.builder()
                .cardNumber(dtoIn.getCardNumber())
                .cardHolder(dtoIn.getCardHolder().toUpperCase())
                .expirationDate(dtoIn.getExpirationDate())
                .balance(dtoIn.getBalance())
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        return mapToDtoOut(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public Page<CardDtoOut> getAllCardsForAdmin(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::mapToDtoOut);
    }

    @Transactional
    public CardDtoOut updateStatusByAdmin(Long id, CardStatus newStatus) {
        Card card = cardRepository.byId(id);
        card.setStatus(newStatus);
        return mapToDtoOut(cardRepository.save(card));
    }

    @Transactional
    public void deleteCardByAdmin(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new NotFoundException("Card not found");
        }
        cardRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<CardDtoOut> getMyCards(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable).map(this::mapToDtoOut);
    }

    @Transactional
    public CardDtoOut requestBlockCard(Long id, Long userId) {
        Card card = cardRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Card not found or doesn't belong to you"));

        card.setStatus(CardStatus.BLOCKED);
        return mapToDtoOut(cardRepository.save(card));
    }

    @Transactional
    public void transferBetweenOwnCards(Long userId, TransferDtoIn transferDto) {
        Card fromCard = cardRepository.findByIdAndUserId(transferDto.getFromCardId(), userId)
                .orElseThrow(() -> new NotFoundException("Source card not found"));

        Card toCard = cardRepository.findByIdAndUserId(transferDto.getToCardId(), userId)
                .orElseThrow(() -> new NotFoundException("Destination card not found"));

        if (fromCard.getId().equals(toCard.getId())) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new BadRequestException("Both cards must be ACTIVE to perform a transfer");
        }

        if (fromCard.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new BadRequestException("Insufficient funds");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(transferDto.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferDto.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
