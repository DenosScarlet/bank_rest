package com.denos.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию нового пользователя")
public class UserRegisterRequest {

    @Schema(
            description = "Уникальное имя пользователя", example = "ivan_petrov"
    )
    private String username;

    @Schema(
            description = "Пароль", example = "securePassword123"
    )
    private String password;

    @Schema(
            description = "Имя пользователя", example = "Иван"
    )
    private String firstName;

    @Schema(
            description = "Фамилия пользователя", example = "Петров"
    )
    private String lastName;

    @Schema(
            description = "Отчество пользователя", example = "Иванович"
    )
    private String middleName;
}
