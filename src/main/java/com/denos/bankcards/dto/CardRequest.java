package com.denos.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на создание банковской карты")
public class CardRequest {

    @Schema(
            description = "ID пользователя, к которому привязать карту", example = "1"
    )
    private Long userId;

    @Schema(
            description = "Номер карты", example = "4111111111111111"
    )
    private String cardNumber;

    @Schema(
            description = "Имя владельца карты", example = "IVAN PETROV"
    )
    private String ownerName;

    @Schema(
            description = "Месяц окончания срока действия", example = "12"
    )
    private Integer expiryMonth;

    @Schema(
            description = "Год окончания срока действия", example = "2025"
    )
    private Integer expiryYear;
}
