package com.denos.bankcards.controller;

import com.denos.bankcards.dto.CardDto;
import com.denos.bankcards.dto.CardRequest;
import com.denos.bankcards.entity.Card;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.enums.CardStatus;
import com.denos.bankcards.repository.CardRepository;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.util.CryptoUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CryptoUtil cryptoUtil;

    public CardController(CardRepository cardRepository, UserRepository userRepository, CryptoUtil cryptoUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cryptoUtil = cryptoUtil;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(card -> {
                    String decrypted = cryptoUtil.decrypt(card.getCardNumberEncrypted());
                    return CardDto.fromEntity(card, CryptoUtil.maskCardNumber(decrypted));
                })
                .toList();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<CardDto> getMyCards(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return cardRepository.findByUser(user, pageable)
                .map(card -> {
                    String decrypted = cryptoUtil.decrypt(card.getCardNumberEncrypted());
                    return CardDto.fromEntity(card, CryptoUtil.maskCardNumber(decrypted));
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardDto createCard(@RequestBody CardRequest req) {
        User user = userRepository.findById(req.getUserId()).orElseThrow();
        Card card = Card.builder()
                .ownerName(req.getOwnerName())
                .expiryMonth(req.getExpiryMonth())
                .expiryYear(req.getExpiryYear())
                .cardNumberEncrypted(cryptoUtil.encrypt(req.getCardNumber()))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        cardRepository.save(card);
        return CardDto.fromEntity(card, CryptoUtil.maskCardNumber(req.getCardNumber()));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/block")
    public void blockCard(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Card card = cardRepository.findById(id).orElseThrow();

        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !card.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Нет доступа к этой карте");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        cardRepository.deleteById(id);
    }
}
