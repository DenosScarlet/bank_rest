package com.denos.bankcards.controller;

import com.denos.bankcards.dto.AuthRequest;
import com.denos.bankcards.dto.AuthResponse;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req) {
        User u = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        var roles = u.getUserRoles().stream().map(r -> r.getRoleName().name()).collect(Collectors.toList());
        String token = jwtUtil.generateToken(u.getUsername(), roles);
        return new AuthResponse(token);
    }
}
