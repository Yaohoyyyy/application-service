package com.application.service;

import com.application.entity.Card;
import com.application.entity.User;
import com.application.model.CardDto;
import com.application.repository.CardRepository;
import com.application.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    /**
     * Кэшируемое получение карты для API.
     * Первый вызов — идёт в БД и кладёт результат в Redis.
     * Последующие — берут из Redis, метод не выполняется.
     */
    @Cacheable(value = "cards", key = "#id")
    public CardDto getCardDtoById(Long id) {
        return CardDto.fromEntity(findCardById(id));
    }

    @Transactional
    public Card createCard(CardDto cardDto) {
        User user = getUser(cardDto.userId());
        Card card = new Card();
        card.setBalance(cardDto.balance());
        card.setCardType(cardDto.cardType());
        card.setUser(user);
        return cardRepository.save(card);
    }

    /**
     * @CachePut — обновляет значение в кэше после успешного выполнения.
     * Возвращаемый CardDto попадёт в Redis под ключом "cards::<id>".
     */
    @Transactional
    @CachePut(value = "cards", key = "#id")
    public CardDto updateCard(Long id, CardDto cardDto) {
        Card card = findCardById(id);
        User user = getUser(cardDto.userId());
        card.setBalance(cardDto.balance());
        card.setCardType(cardDto.cardType());
        card.setUser(user);
        return CardDto.fromEntity(cardRepository.save(card));
    }

    /**
     * @CacheEvict — удаляет запись из кэша.
     */
    @Transactional
    @CacheEvict(value = "cards", key = "#id")
    public void deleteCard(Long id) {
        Card card = findCardById(id);
        cardRepository.delete(card);
    }

    /** Внутренний метод — НЕ кэшируется, используется в update/delete. */
    private Card findCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("card not found with id: " + id));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}