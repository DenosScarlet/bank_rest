package com.denos.bankcards.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.security.SecureRandom;

@Component
public class CryptoUtil {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int TAGLEN_BIT = 128;
    private static final int IV_LEN_BYTE = 12;

    private final byte[] key;

    public CryptoUtil(@Value("${security.jwt.secret}") String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.key = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to init key", e);
        }
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LEN_BYTE];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAGLEN_BIT, iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            byte[] encrypted = cipher.doFinal(plain.getBytes());
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            byte[] iv = new byte[IV_LEN_BYTE];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            byte[] enc = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, enc, 0, enc.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAGLEN_BIT, iv);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] decrypted = cipher.doFinal(enc);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String maskCardNumber(String plainNumber) {
        String digits = plainNumber.replaceAll("\\D", "");
        if (digits.length() < 4) return "****";
        String last4 = digits.substring(digits.length()-4);
        return "**** **** **** " + last4;
    }
}