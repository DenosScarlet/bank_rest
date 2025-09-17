package com.denos.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
