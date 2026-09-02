package com.application.model;

import com.application.entity.Card;
import com.application.entity.CardType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CardDto(
        Long id,
        @NotNull(message = "Balance is required") BigDecimal balance,
        @NotNull(message = "Card type is required") CardType cardType,
        @NotNull(message = "User id is required") Long userId
) {
    public static CardDto fromEntity(Card card) {
        return new CardDto(
                card.getId(),
                card.getBalance(),
                card.getCardType(),
                card.getUser() != null ? card.getUser().getId() : null
        );
    }
}