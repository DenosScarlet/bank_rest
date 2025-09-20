package com.denos.bankcards.controller;

import com.denos.bankcards.dto.TransferRequest;
import com.denos.bankcards.entity.Card;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.repository.CardRepository;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.util.CryptoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private TransferController transferController;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .build();

        fromCard = Card.builder()
                .id(1L)
                .cardNumberEncrypted("encryptedFromCard")
                .ownerName("PETYA IVANOV")
                .expiryMonth(12)
                .expiryYear(2025)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .build();

        toCard = Card.builder()
                .id(2L)
                .cardNumberEncrypted("encryptedToCard")
                .ownerName("PETYA IVANOV")
                .expiryMonth(12)
                .expiryYear(2025)
                .balance(BigDecimal.valueOf(500))
                .user(testUser)
                .build();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void transfer_ValidTransfer_ReturnsSuccessMessage() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cryptoUtil.decrypt("encryptedFromCard")).thenReturn("1234567890123456");
        when(cryptoUtil.decrypt("encryptedToCard")).thenReturn("9876543210987654");

        // Act
        String result = transferController.transfer(userDetails, request);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("100"));
        assertEquals(BigDecimal.valueOf(900), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(600), toCard.getBalance());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).findById(2L);
        verify(cryptoUtil, times(1)).decrypt("encryptedFromCard");
        verify(cryptoUtil, times(1)).decrypt("encryptedToCard");
    }

    @Test
    void transfer_InsufficientBalance_ThrowsException() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(1500));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                transferController.transfer(userDetails, request));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).findById(2L);
    }

    @Test
    void transfer_NotOwnCard_ThrowsAccessDenied() {
        // Arrange
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .password("encodedPassword")
                .build();

        Card foreignCard = Card.builder()
                .id(3L)
                .cardNumberEncrypted("encryptedForeignCard")
                .ownerName("Another User")
                .expiryMonth(12)
                .expiryYear(2025)
                .balance(BigDecimal.valueOf(1000))
                .user(anotherUser)
                .build();

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(3L);
        request.setAmount(BigDecimal.valueOf(100));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(foreignCard));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                transferController.transfer(userDetails, request));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).findById(3L);
    }
}