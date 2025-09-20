package com.denos.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на перевод средств")
public class TransferRequest {
    @Schema(description = "ID карты отправителя", example = "1")
    private Long fromCardId;

    @Schema(description = "ID карты получателя", example = "2")
    private Long toCardId;

    @Schema(description = "Сумма перевода", example = "100.00")
    private BigDecimal amount;
}