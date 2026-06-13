package com.example.bankcards.controller.card;

import com.example.bankcards.dto.card.BlockRequestDtoOut;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.card.TransferDtoIn;
import com.example.bankcards.service.card.CardService;
import com.example.bankcards.util.enums.card.CardStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management Controller", description = "Bank card operations (ADMIN/USER rights separation)")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @GetMapping("/my")
    @Operation(summary = "Viewing your cards (Search + pagination)")
    public Page<CardDtoOut> getMyCards(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return cardService.getMyCards(userId, pageable);
    }

    @PostMapping("/my/transfer")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Transfer between your own cards")
    public void transferBetweenOwnCards(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody TransferDtoIn transferDto) {
        cardService.transferBetweenOwnCards(userId, transferDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[ADMIN] Create a new card for the user")
    public CardDtoOut createCard(@Valid @RequestBody CardDtoIn dtoIn) {
        return cardService.createCard(dtoIn);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] View all cards in the system")
    public Page<CardDtoOut> getAllCardsForAdmin(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return cardService.getAllCardsForAdmin(pageable);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Change card status (ACTIVE, BLOCKED, EXPIRED)")
    public CardDtoOut changeCardStatus(
            @PathVariable Long id,
            @RequestParam CardStatus status) {
        return cardService.updateStatusByAdmin(id, status);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "[ADMIN] Remove card from the system")
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCardByAdmin(id);
    }

    @PostMapping("/my/{id}/block")
    @Operation(summary = "Create request to have a bank employee block the card")
    public BlockRequestDtoOut requestBlockCard(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false, defaultValue = "Not specified") String reason) {
        return cardService.createBlockRequest(id, userId, reason);
    }

    @GetMapping("/block-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] View list of all pending blocking requests")
    public Page<BlockRequestDtoOut> getPendingRequests(
            @PageableDefault(size = 10) Pageable pageable) {
        return cardService.getAllPendingRequests(pageable);
    }

    @PatchMapping("/block-requests/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Approve (approve=true) or reject (approve=false) the blocking request")
    public BlockRequestDtoOut processRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approve) {
        return cardService.processBlockRequest(requestId, approve);
    }

    @GetMapping("/my/block-requests")
    @Operation(summary = "View the history of card blocking requests (with pagination)")
    public Page<BlockRequestDtoOut> getMyBlockRequests(
            @RequestAttribute("userId") Long userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return cardService.getMyBlockRequests(userId, pageable);
    }

}