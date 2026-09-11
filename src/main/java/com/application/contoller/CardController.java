package com.application.contoller;

import com.application.model.CardDto;
import com.application.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<CardDto> getAllCards() {
        return cardService.getAllCards()
                .stream()
                .map(CardDto::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public CardDto getCardById(@PathVariable Long id) {
        return cardService.getCardDtoById(id);   // уже DTO из кэша/БД
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCard(@Valid @RequestBody CardDto cardDto) {
        return CardDto.fromEntity(cardService.createCard(cardDto));
    }

    @PutMapping("/{id}")
    public CardDto updateCard(@PathVariable Long id, @Valid @RequestBody CardDto cardDto) {
        return cardService.updateCard(id, cardDto);   // уже DTO
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
    }
}