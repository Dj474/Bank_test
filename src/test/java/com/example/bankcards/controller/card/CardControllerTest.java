package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.TransferDtoIn;
import com.example.bankcards.entity.card.BlockRequest;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.repository.card.BlockRequestRepository;
import com.example.bankcards.repository.card.CardRepository;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.util.enums.card.BlockRequestStatus;
import com.example.bankcards.util.enums.card.CardStatus;
import com.example.bankcards.util.enums.user.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockRequestRepository blockRequestRepository;

    private User testUser;
    private Card firstCard;
    private Card secondCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("card_holder_user")
                .password("secure_password_123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        LocalDateTime now = LocalDateTime.now();

        firstCard = Card.builder()
                .cardNumber("1111222233334444")
                .cardHolder("IVAN IVANOV")
                .balance(new BigDecimal("1000.00"))
                .status(CardStatus.ACTIVE)
                .createdAt(now)
                .expirationDate(now.plusYears(3).toLocalDate())
                .user(testUser)
                .build();
        firstCard = cardRepository.save(firstCard);

        secondCard = Card.builder()
                .cardNumber("5555666677778888")
                .cardHolder("IVAN IVANOV")
                .balance(new BigDecimal("200.00"))
                .status(CardStatus.ACTIVE)
                .createdAt(now)
                .expirationDate(now.plusYears(3).toLocalDate())
                .user(testUser)
                .build();
        secondCard = cardRepository.save(secondCard);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyCards_Success() throws Exception {
        mockMvc.perform(get("/api/v1/cards/my")
                        .requestAttr("userId", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].cardNumber").value("**** **** **** 4444"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void transferBetweenOwnCards_Success() throws Exception {
        TransferDtoIn transferDto = new TransferDtoIn();
        transferDto.setFromCardId(firstCard.getId());
        transferDto.setToCardId(secondCard.getId());
        transferDto.setAmount(new BigDecimal("300.00"));

        mockMvc.perform(post("/api/v1/cards/my/transfer")
                        .requestAttr("userId", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk());

        Card updatedFromCard = cardRepository.findById(firstCard.getId()).orElseThrow();
        Card updatedToCard = cardRepository.findById(secondCard.getId()).orElseThrow();

        assertThat(updatedFromCard.getBalance()).isEqualByComparingTo("700.00");
        assertThat(updatedToCard.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @WithMockUser(roles = "USER")
    void requestBlockCard_Success() throws Exception {
        mockMvc.perform(post("/api/v1/cards/my/{id}/block", firstCard.getId())
                        .requestAttr("userId", testUser.getId())
                        .param("reason", "Lost my wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        boolean requestExists = blockRequestRepository.existsByCardIdAndStatus(firstCard.getId(), BlockRequestStatus.PENDING);
        assertThat(requestExists).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_AsAdmin_Success() throws Exception {
        CardDtoIn dtoIn = new CardDtoIn();
        dtoIn.setCardNumber("9999888877776666");
        dtoIn.setCardHolder("MARIA IVANOVA");
        dtoIn.setExpirationDate(LocalDate.now().plusYears(3));
        dtoIn.setUserId(testUser.getId());

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoIn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 6666"));

        assertThat(cardRepository.existsByCardNumber("9999888877776666")).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeCardStatus_AsAdmin_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/cards/{id}/status", firstCard.getId())
                        .param("status", "BLOCKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        Card updatedCard = cardRepository.findById(firstCard.getId()).orElseThrow();
        assertThat(updatedCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void processBlockRequest_Approve_AsAdmin_Success() throws Exception {
        BlockRequest pendingRequest = BlockRequest.builder()
                .card(firstCard)
                .user(testUser)
                .status(BlockRequestStatus.PENDING)
                .reason("Stolen")
                .createdAt(LocalDateTime.now())
                .build();
        pendingRequest = blockRequestRepository.save(pendingRequest);

        mockMvc.perform(patch("/api/v1/cards/block-requests/{requestId}", pendingRequest.getId())
                        .param("approve", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Card updatedCard = cardRepository.findById(firstCard.getId()).orElseThrow();
        assertThat(updatedCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_AsUser_ReturnsForbidden() throws Exception {
        CardDtoIn dtoIn = new CardDtoIn();
        dtoIn.setCardNumber("1234123412341234");
        dtoIn.setCardHolder("VALID HOLDER");
        dtoIn.setExpirationDate(LocalDate.now().plusYears(3));
        dtoIn.setUserId(testUser.getId());

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoIn)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/cards/{id}", firstCard.getId()))
                .andExpect(status().isForbidden());
    }
}