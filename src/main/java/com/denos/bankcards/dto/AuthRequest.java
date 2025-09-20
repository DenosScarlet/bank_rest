package com.denos.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос аутентификации")
public class AuthRequest {
    @Schema(description = "Имя пользователя", example = "user1")
    private String username;

    @Schema(description = "Пароль", example = "user123")
    private String password;
}
