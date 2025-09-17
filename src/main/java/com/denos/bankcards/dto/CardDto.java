package com.denos.bankcards.dto;

import com.denos.bankcards.entity.Card;
import com.denos.bankcards.enums.CardStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardDto {
    private Long id;
    private String maskedNumber;
    private String ownerName;
    private Integer expiryMonth;
    private Integer expiryYear;
    private CardStatus status;
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
