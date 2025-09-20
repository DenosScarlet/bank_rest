package com.denos.bankcards.controller;

import com.denos.bankcards.dto.TransferRequest;
import com.denos.bankcards.entity.Card;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.repository.CardRepository;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.util.CryptoUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CryptoUtil cryptoUtil;

    public TransferController(CardRepository cardRepository, UserRepository userRepository, CryptoUtil cryptoUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cryptoUtil = cryptoUtil;
    }

    @Operation(
            summary = "Перевод между картами",
            description = "Выполняет перевод средств между картами текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод выполнен успешно",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств или некорректные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ к картам запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    @Transactional
    public String transfer(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestBody TransferRequest req) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Card from = cardRepository.findById(req.getFromCardId()).orElseThrow();
        Card to = cardRepository.findById(req.getToCardId()).orElseThrow();

        if (!from.getUser().equals(user) || !to.getUser().equals(user)) {
            throw new AccessDeniedException("Вы можете переводить только между своими картами");
        }

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

