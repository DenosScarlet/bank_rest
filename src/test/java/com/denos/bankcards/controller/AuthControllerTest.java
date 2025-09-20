package com.denos.bankcards.controller;

import com.denos.bankcards.dto.AuthRequest;
import com.denos.bankcards.dto.AuthResponse;
import com.denos.bankcards.entity.Role;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.enums.RoleType;
import com.denos.bankcards.repository.UserRepository;
import com.denos.bankcards.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .enabled(true)
                .userRoles(Set.of(new Role(1L, RoleType.ROLE_USER)))
                .build();
    }

    @Test
    void login_ValidCredentials_ReturnsToken() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(any(), any())).thenReturn("test-token");

        // Act
        AuthResponse response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password", "encodedPassword");
        verify(jwtUtil, times(1)).generateToken(any(), any());
    }

    @Test
    void login_InvalidUsername_ThrowsException() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("wronguser");
        request.setPassword("password");

        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authController.login(request));
        verify(userRepository, times(1)).findByUsername("wronguser");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authController.login(request));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedPassword");
    }
}