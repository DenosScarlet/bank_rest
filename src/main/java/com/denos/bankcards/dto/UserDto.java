package com.denos.bankcards.dto;

import com.denos.bankcards.entity.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String middleName;
    private boolean enabled;

    public static UserDto fromEntity(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFirstName(u.getFirstName());
        dto.setLastName(u.getLastName());
        dto.setMiddleName(u.getMiddleName());
        dto.setEnabled(u.isEnabled());
        return dto;
    }
}
