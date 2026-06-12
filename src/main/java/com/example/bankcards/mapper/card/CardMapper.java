package com.example.bankcards.mapper.card;

import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.card.BlockRequestDtoOut;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.BlockRequest;
import com.example.bankcards.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "cardHolder", expression = "java(dtoIn.getCardHolder().toUpperCase())")
    @Mapping(target = "balance", source = "dtoIn.balance", defaultValue = "0")
    @Mapping(target = "status", expression = "java(com.example.bankcards.util.enums.card.CardStatus.ACTIVE)")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Card toEntity(CardDtoIn dtoIn, User user);

    @Mapping(target = "cardNumber", source = "maskedCardNumber")
    @Mapping(target = "userId", source = "user.id")
    CardDtoOut toDtoOut(Card card);

    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "maskedCardNumber", source = "card.maskedCardNumber")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    BlockRequestDtoOut toBlockRequestDtoOut(BlockRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card", source = "card")
    @Mapping(target = "user", source = "card.user")
    @Mapping(target = "status", expression = "java(com.example.bankcards.util.enums.card.BlockRequestStatus.PENDING)")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "processedAt", ignore = true)
    BlockRequest toBlockRequestEntity(Card card, String reason);
}