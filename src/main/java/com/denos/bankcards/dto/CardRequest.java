package com.denos.bankcards.dto;

import lombok.Data;

@Data
public class CardRequest {
    private Long userId;
    private String cardNumber;
    private String ownerName;
    private Integer expiryMonth;
    private Integer expiryYear;
}
