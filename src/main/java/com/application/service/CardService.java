package com.application.service;

import com.application.entity.Card;
import com.application.entity.User;
import com.application.model.CardDto;
import com.application.repository.CardRepository;
import com.application.repository.UserRepository;
import jakarta.transaction.Transactional;
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

    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("card not found with id: " + id));
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

    @Transactional
    public Card updateCard(Long id, CardDto cardDto) {
        Card card = getCardById(id);
        User user = getUser(cardDto.userId());
        card.setBalance(cardDto.balance());
        card.setCardType(cardDto.cardType());
        card.setUser(user);
        return cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = getCardById(id);
        cardRepository.delete(card);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}