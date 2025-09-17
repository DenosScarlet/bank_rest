package com.denos.bankcards.controller;

import com.denos.bankcards.dto.TransferRequest;
import com.denos.bankcards.entity.Card;
import com.denos.bankcards.repository.CardRepository;
import com.denos.bankcards.util.CryptoUtil;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final CardRepository cardRepository;
    private final CryptoUtil cryptoUtil;

    public TransferController(CardRepository cardRepository, CryptoUtil cryptoUtil) {
        this.cardRepository = cardRepository;
        this.cryptoUtil = cryptoUtil;
    }

    @PostMapping
    @Transactional
    public String transfer(@RequestBody TransferRequest req) {
        Card from = cardRepository.findById(req.getFromCardId()).orElseThrow();
        Card to = cardRepository.findById(req.getToCardId()).orElseThrow();

        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Недостаточно средств");
        }

        from.setBalance(from.getBalance().subtract(req.getAmount()));
        to.setBalance(to.getBalance().add(req.getAmount()));

        String fromMasked = CryptoUtil.maskCardNumber(cryptoUtil.decrypt(from.getCardNumberEncrypted()));
        String toMasked = CryptoUtil.maskCardNumber(cryptoUtil.decrypt(to.getCardNumberEncrypted()));

        return String.format("Перевод %s₽ выполнен с карты %s на карту %s",
                req.getAmount(), fromMasked, toMasked);
    }
}
