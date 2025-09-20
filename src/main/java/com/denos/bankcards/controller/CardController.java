package com.denos.bankcards.controller;

import com.denos.bankcards.dto.CardDto;
import com.denos.bankcards.dto.CardRequest;
import com.denos.bankcards.entity.Card;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.enums.CardStatus;
import com.denos.bankcards.repository.CardRepository;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.util.CryptoUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CryptoUtil cryptoUtil;

    public CardController(CardRepository cardRepository, UserRepository userRepository, CryptoUtil cryptoUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cryptoUtil = cryptoUtil;
    }

    @Operation(
            summary = "Получение всех карт",
            description = "Возвращает список всех карт в системе. Только для администраторов."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный запрос",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardDto.class)))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(card -> {
                    String decrypted = cryptoUtil.decrypt(card.getCardNumberEncrypted());
                    return CardDto.fromEntity(card, CryptoUtil.maskCardNumber(decrypted));
                })
                .toList();
    }

    @Operation(
            summary = "Получение карт пользователя",
            description = "Возвращает список карт аутентифицированного пользователя с поддержкой пагинации"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный запрос",
                    content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public Page<CardDto> getMyCards(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return cardRepository.findByUser(user, pageable)
                .map(card -> {
                    String decrypted = cryptoUtil.decrypt(card.getCardNumberEncrypted());
                    return CardDto.fromEntity(card, CryptoUtil.maskCardNumber(decrypted));
                });
    }

    @Operation(
            summary = "Создание карты",
            description = "Создает новую банковскую карту. Только для администраторов."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = CardDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardDto createCard(@RequestBody CardRequest req) {
        User user = userRepository.findById(req.getUserId()).orElseThrow();
        Card card = Card.builder()
                .ownerName(req.getOwnerName())
                .expiryMonth(req.getExpiryMonth())
                .expiryYear(req.getExpiryYear())
                .cardNumberEncrypted(cryptoUtil.encrypt(req.getCardNumber()))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();
        cardRepository.save(card);
        return CardDto.fromEntity(card, CryptoUtil.maskCardNumber(req.getCardNumber()));
    }

    @Operation(
            summary = "Блокировка карты",
            description = "Блокирует карту. Пользователи могут блокировать только свои карты, администраторы - любые."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/block")
    public void blockCard(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        Card card = cardRepository.findById(id).orElseThrow();

        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !card.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("Нет доступа к этой карте");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Operation(
            summary = "Удаление карты",
            description = "Удаляет карту по ID. Только для администраторов."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        cardRepository.deleteById(id);
    }
}
