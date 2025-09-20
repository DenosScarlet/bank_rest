package com.denos.bankcards.dto;

import com.denos.bankcards.entity.Card;
import com.denos.bankcards.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Данные карты")
public class CardDto {
    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Замаскированный номер карты", example = "**** **** **** 1234")
    private String maskedNumber;

    @Schema(description = "Имя владельца", example = "IVAN PETROV")
    private String ownerName;

    @Schema(description = "Месяц окончания срока", example = "12")
    private Integer expiryMonth;

    @Schema(description = "Год окончания срока", example = "2025")
    private Integer expiryYear;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс", example = "1000.00")
    private BigDecimal balance;

    public static CardDto fromEntity(Card c, String masked) {
        CardDto d = new CardDto();
        d.setId(c.getId());
        d.setMaskedNumber(masked);
        d.setOwnerName(c.getOwnerName());
        d.setExpiryMonth(c.getExpiryMonth());
        d.setExpiryYear(c.getExpiryYear());
        d.setStatus(c.getStatus());
        d.setBalance(c.getBalance());
        return d;
    }
}
