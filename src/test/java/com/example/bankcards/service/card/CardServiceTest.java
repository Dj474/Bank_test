package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.BlockRequestDtoOut;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.card.TransferDtoIn;
import com.example.bankcards.entity.card.BlockRequest;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.util.mapper.card.CardMapper;
import com.example.bankcards.repository.card.BlockRequestRepository;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.card.BlockRequestStatus;
import com.example.bankcards.util.enums.card.CardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlockRequestRepository blockRequestRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardDtoIn cardDtoIn;
    private CardDtoOut cardDtoOut;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test_user").build();

        card = Card.builder()
                .id(10L)
                .cardNumber("1111222233334444")
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        cardDtoIn = new CardDtoIn();
        cardDtoIn.setCardNumber("1111222233334444");
        cardDtoIn.setUserId(1L);

        cardDtoOut = new CardDtoOut();
        cardDtoOut.setId(10L);
        cardDtoOut.setCardNumber("1111222233334444");
        cardDtoOut.setBalance(new BigDecimal("1000.00"));
        cardDtoOut.setStatus("ACTIVE");
    }

    @Test
    void createCard_Success() {
        when(cardRepository.existsByCardNumber(cardDtoIn.getCardNumber())).thenReturn(false);
        when(userRepository.byId(cardDtoIn.getUserId())).thenReturn(user);
        when(cardMapper.toEntity(cardDtoIn, user)).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(cardMapper.toDtoOut(card)).thenReturn(cardDtoOut);

        CardDtoOut result = cardService.createCard(cardDtoIn);

        assertThat(result).isNotNull();
        assertThat(result.getCardNumber()).isEqualTo("1111222233334444");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_ThrowsBadRequest_WhenCardNumberExists() {
        when(cardRepository.existsByCardNumber(cardDtoIn.getCardNumber())).thenReturn(true);

        assertThatThrownBy(() -> cardService.createCard(cardDtoIn))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Card with this number already exists");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getMyCards_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(card));

        when(cardRepository.findByUserId(1L, pageable)).thenReturn(cardPage);
        when(cardMapper.toDtoOut(card)).thenReturn(cardDtoOut);

        Page<CardDtoOut> result = cardService.getMyCards(1L, pageable);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
    }

    @Test
    void transferBetweenOwnCards_Success() {
        TransferDtoIn transferDto = new TransferDtoIn();
        transferDto.setFromCardId(10L);
        transferDto.setToCardId(20L);
        transferDto.setAmount(new BigDecimal("300.00"));

        Card toCard = Card.builder()
                .id(20L)
                .cardNumber("5555666677778888")
                .balance(new BigDecimal("200.00"))
                .status(CardStatus.ACTIVE)
                .user(user)
                .build();

        when(cardRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(card));
        when(cardRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(toCard));

        cardService.transferBetweenOwnCards(1L, transferDto);

        assertThat(card.getBalance()).isEqualByComparingTo("700.00");
        assertThat(toCard.getBalance()).isEqualByComparingTo("500.00");

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_ThrowsBadRequest_WhenSameCard() {
        TransferDtoIn transferDto = new TransferDtoIn();
        transferDto.setFromCardId(10L);
        transferDto.setToCardId(10L);
        transferDto.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.transferBetweenOwnCards(1L, transferDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot transfer to the same card");
    }

    @Test
    void transfer_ThrowsBadRequest_WhenInsufficientFunds() {
        TransferDtoIn transferDto = new TransferDtoIn();
        transferDto.setFromCardId(10L);
        transferDto.setToCardId(20L);
        transferDto.setAmount(new BigDecimal("1500.00"));

        Card toCard = Card.builder().id(20L).status(CardStatus.ACTIVE).balance(BigDecimal.ZERO).build();

        when(cardRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(card));
        when(cardRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(toCard));

        assertThatThrownBy(() -> cardService.transferBetweenOwnCards(1L, transferDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void createBlockRequest_Success() {
        BlockRequest mockRequest = new BlockRequest();
        BlockRequestDtoOut mockDtoOut = new BlockRequestDtoOut();

        when(cardRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(card));
        when(blockRequestRepository.existsByCardIdAndStatus(10L, BlockRequestStatus.PENDING)).thenReturn(false);
        when(cardMapper.toBlockRequestEntity(card, "Stolen")).thenReturn(mockRequest);
        when(blockRequestRepository.save(mockRequest)).thenReturn(mockRequest);
        when(cardMapper.toBlockRequestDtoOut(mockRequest)).thenReturn(mockDtoOut);

        BlockRequestDtoOut result = cardService.createBlockRequest(10L, 1L, "Stolen");

        assertThat(result).isNotNull();
        verify(blockRequestRepository).save(mockRequest);
    }

    @Test
    void processBlockRequest_Approve_Success() {
        BlockRequest request = new BlockRequest();
        request.setId(100L);
        request.setStatus(BlockRequestStatus.PENDING);
        request.setCard(card);

        BlockRequestDtoOut dtoOut = new BlockRequestDtoOut();

        when(blockRequestRepository.findById(100L)).thenReturn(Optional.of(request));
        when(blockRequestRepository.save(any(BlockRequest.class))).thenReturn(request);
        when(cardMapper.toBlockRequestDtoOut(any(BlockRequest.class))).thenReturn(dtoOut);

        BlockRequestDtoOut result = cardService.processBlockRequest(100L, true);

        assertThat(result).isNotNull();
        assertThat(request.getStatus()).isEqualTo(BlockRequestStatus.APPROVED);
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);

        verify(cardRepository).save(card);
        verify(blockRequestRepository).save(request);
    }
}