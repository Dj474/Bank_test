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
import com.example.bankcards.util.mapper.card.CardMapper;
import com.example.bankcards.repository.card.BlockRequestRepository;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.card.BlockRequestStatus;
import com.example.bankcards.util.enums.card.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final BlockRequestRepository blockRequestRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardDtoOut createCard(CardDtoIn dtoIn) {
        if (cardRepository.existsByCardNumber(dtoIn.getCardNumber())) {
            throw new BadRequestException("Card with this number already exists");
        }

        User user = userRepository.byId(dtoIn.getUserId());

        Card card = cardMapper.toEntity(dtoIn, user);

        return cardMapper.toDtoOut(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public Page<CardDtoOut> getAllCardsForAdmin(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDtoOut);
    }

    @Transactional
    public CardDtoOut updateStatusByAdmin(Long id, CardStatus newStatus) {
        Card card = cardRepository.byId(id);
        card.setStatus(newStatus);
        return cardMapper.toDtoOut(cardRepository.save(card));
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
        return cardRepository.findByUserId(userId, pageable).map(cardMapper::toDtoOut);
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

        BlockRequest request = cardMapper.toBlockRequestEntity(card, reason);

        return cardMapper.toBlockRequestDtoOut(blockRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<BlockRequestDtoOut> getAllPendingRequests(Pageable pageable) {
        return blockRequestRepository.findByStatus(BlockRequestStatus.PENDING, pageable)
                .map(cardMapper::toBlockRequestDtoOut);
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
        return cardMapper.toBlockRequestDtoOut(blockRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<BlockRequestDtoOut> getMyBlockRequests(Long userId, Pageable pageable) {
        return blockRequestRepository.findByUserId(userId, pageable)
                .map(cardMapper::toBlockRequestDtoOut);
    }

}
