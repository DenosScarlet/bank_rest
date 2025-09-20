package com.denos.bankcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    private CryptoUtil cryptoUtil;

    @BeforeEach
    void setUp() {
        cryptoUtil = new CryptoUtil("test-secret-key-for-encryption-test");
    }

    @Test
    void encryptAndDecrypt_ValidData_ReturnsOriginalValue() {
        // Arrange
        String originalText = "1234567890123456"; // Card number

        // Act
        String encrypted = cryptoUtil.encrypt(originalText);
        String decrypted = cryptoUtil.decrypt(encrypted);

        // Assert
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);
        assertEquals(originalText, decrypted);
    }

    @Test
    void maskCardNumber_ValidCardNumber_ReturnsMasked() {
        // Arrange
        String cardNumber = "1234567890123456";

        // Act
        String masked = CryptoUtil.maskCardNumber(cardNumber);

        // Assert
        assertNotNull(masked);
        assertEquals("**** **** **** 3456", masked);
    }

    @Test
    void maskCardNumber_ShortCardNumber_ReturnsMasked() {
        // Arrange
        String cardNumber = "1234";

        // Act
        String masked = CryptoUtil.maskCardNumber(cardNumber);

        // Assert
        assertNotNull(masked);
        assertEquals("**** **** **** 1234", masked);
    }

    @Test
    void maskCardNumber_CardNumberWithSpaces_ReturnsMasked() {
        // Arrange
        String cardNumber = "1234 5678 9012 3456";

        // Act
        String masked = CryptoUtil.maskCardNumber(cardNumber);

        // Assert
        assertNotNull(masked);
        assertEquals("**** **** **** 3456", masked);
    }
}