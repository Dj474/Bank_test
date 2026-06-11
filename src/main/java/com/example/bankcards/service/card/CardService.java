package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.BlockRequestDtoOut;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.card.TransferDtoIn;
import com.example.bankcards.entity.card.BlockRequest;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.card.BlockRequestRepository;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.card.BlockRequestStatus;
import com.example.bankcards.util.enums.card.CardStatus;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BlockRequestRepository blockRequestRepository;

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
        if (cardRepository.existsByCardNumber(dtoIn.getCardNumber())) {
            throw new BadRequestException("Card with this number already exists");
        }

        User user = userRepository.byId(dtoIn.getUserId());

        Card card = Card.builder()
                .cardNumber(dtoIn.getCardNumber())
                .cardHolder(dtoIn.getCardHolder().toUpperCase())
                .expirationDate(dtoIn.getExpirationDate())
                .balance(dtoIn.getBalance() != null ? dtoIn.getBalance() : java.math.BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
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

    @Transactional
    public BlockRequestDtoOut createBlockRequest(Long cardId, Long userId, String reason) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BadRequestException("Card is already blocked");
        }

        if (blockRequestRepository.existsByCardIdAndStatus(cardId, BlockRequestStatus.PENDING)) {
            throw new BadRequestException("A block request for this card is already pending review");
        }

        BlockRequest request = BlockRequest.builder()
                .card(card)
                .user(card.getUser())
                .status(BlockRequestStatus.PENDING)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToBlockRequestDto(blockRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<BlockRequestDtoOut> getAllPendingRequests(Pageable pageable) {
        return blockRequestRepository.findByStatus(BlockRequestStatus.PENDING, pageable)
                .map(this::mapToBlockRequestDto);
    }

    @Transactional
    public BlockRequestDtoOut processBlockRequest(Long requestId, boolean approve) {
        BlockRequest request = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (request.getStatus() != BlockRequestStatus.PENDING) {
            throw new BadRequestException("Request is already processed");
        }

        if (approve) {
            request.setStatus(BlockRequestStatus.APPROVED);
            Card card = request.getCard();
            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        } else {
            request.setStatus(BlockRequestStatus.REJECTED);
        }

        request.setProcessedAt(LocalDateTime.now());
        return mapToBlockRequestDto(blockRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<BlockRequestDtoOut> getMyBlockRequests(Long userId, Pageable pageable) {
        return blockRequestRepository.findByUserId(userId, pageable)
                .map(this::mapToBlockRequestDto);
    }

    private BlockRequestDtoOut mapToBlockRequestDto(BlockRequest req) {
        BlockRequestDtoOut dto = new BlockRequestDtoOut();
        dto.setId(req.getId());
        dto.setCardId(req.getCard().getId());
        dto.setMaskedCardNumber(req.getCard().getMaskedCardNumber());
        dto.setUserId(req.getUser().getId());
        dto.setUsername(req.getUser().getUsername());
        dto.setStatus(req.getStatus().name());
        dto.setReason(req.getReason());
        dto.setCreatedAt(req.getCreatedAt());
        dto.setProcessedAt(req.getProcessedAt());
        return dto;
    }

}
