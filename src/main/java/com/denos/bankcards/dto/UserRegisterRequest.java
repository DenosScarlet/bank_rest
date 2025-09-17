package com.denos.bankcards.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String middleName;
}
