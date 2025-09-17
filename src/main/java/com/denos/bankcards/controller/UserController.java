package com.denos.bankcards.controller;

import com.denos.bankcards.dto.UserDto;
import com.denos.bankcards.dto.UserRegisterRequest;
import com.denos.bankcards.entity.Role;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.repository.RoleRepository;
import com.denos.bankcards.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = encoder;
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody UserRegisterRequest req) {
        var user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .middleName(req.getMiddleName())
                .enabled(true)
                .userRoles(Set.of(roleRepository.findByRoleName("ROLE_USER").orElseThrow()))
                .build();
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @GetMapping("/me")
    public UserDto me(Principal principal) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();
        return UserDto.fromEntity(user);
    }

    @GetMapping
    public java.util.List<UserDto> all() {
        return userRepository.findAll().stream().map(UserDto::fromEntity).toList();
    }
}