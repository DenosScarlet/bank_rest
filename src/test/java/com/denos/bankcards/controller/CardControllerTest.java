package com.denos.bankcards.controller;

import com.denos.bankcards.dto.CardDto;
import com.denos.bankcards.dto.CardRequest;
import com.denos.bankcards.entity.Card;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.enums.CardStatus;
import com.denos.bankcards.repository.CardRepository;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private CardController cardController;

    private User testUser;
    private Card testCard;
    private UserDetails userDetails;
    private UserDetails adminDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .build();

        testCard = Card.builder()
                .id(1L)
                .cardNumberEncrypted("encryptedCardNumber")
                .ownerName("PETYA IVANOV")
                .expiryMonth(12)
                .expiryYear(2025)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }

    @Test
    void getMyCards_UserCards_ReturnsPage() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findByUser(testUser, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(testCard)));
        when(cryptoUtil.decrypt("encryptedCardNumber")).thenReturn("1234567890123456");

        // Act
        Page<CardDto> result = cardController.getMyCards(userDetails, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cardRepository, times(1)).findByUser(testUser, Pageable.unpaged());
        verify(cryptoUtil, times(1)).decrypt("encryptedCardNumber");
    }

    @Test
    void createCard_AdminUser_CreatesCard() {
        // Arrange
        CardRequest request = new CardRequest();
        request.setUserId(1L);
        request.setCardNumber("1234567890123456");
        request.setOwnerName("PETYA IVANOV");
        request.setExpiryMonth(12);
        request.setExpiryYear(2025);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cryptoUtil.encrypt("1234567890123456")).thenReturn("encryptedCardNumber");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(2L);
            return card;
        });

        // Act
        CardDto result = cardController.createCard(request);

        // Assert
        assertNotNull(result);
        assertEquals("PETYA IVANOV", result.getOwnerName());
        verify(userRepository, times(1)).findById(1L);
        verify(cryptoUtil, times(1)).encrypt("1234567890123456");
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void blockCard_UserOwnsCard_BlocksCard() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardController.blockCard(userDetails, 1L);

        // Assert
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void blockCard_UserDoesNotOwnCard_ThrowsAccessDenied() {
        // Arrange
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .password("encodedPassword")
                .build();

        Card anotherCard = Card.builder()
                .id(2L)
                .cardNumberEncrypted("encryptedCardNumber")
                .ownerName("Another User")
                .expiryMonth(12)
                .expiryYear(2025)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .user(anotherUser)
                .build();

        when(cardRepository.findById(2L)).thenReturn(Optional.of(anotherCard));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                cardController.blockCard(userDetails, 2L));
        verify(cardRepository, times(1)).findById(2L);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_AdminUser_BlocksAnyCard() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardController.blockCard(adminDetails, 1L);

        // Assert
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(testCard);
    }

    @Test
    void deleteCard_AdminUser_DeletesCard() {
        // Act
        cardController.deleteCard(1L);

        // Assert
        verify(cardRepository, times(1)).deleteById(1L);
    }
}