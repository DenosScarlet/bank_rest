package com.denos.bankcards.controller;

import com.denos.bankcards.dto.UserDto;
import com.denos.bankcards.dto.UserRegisterRequest;
import com.denos.bankcards.entity.Role;
import com.denos.bankcards.entity.User;
import com.denos.bankcards.enums.RoleType;
import com.denos.bankcards.repository.RoleRepository;
import com.denos.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, RoleType.ROLE_USER);
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .firstName("Петя")
                .lastName("Иванов")
                .middleName("Петрович")
                .enabled(true)
                .userRoles(Set.of(userRole))
                .build();
    }

    @Test
    void register_ValidRequest_ReturnsUserDto() {
        // Arrange
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        request.setFirstName("Петя");
        request.setLastName("Иванов");
        request.setMiddleName("Петрович");

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        // Act
        UserDto result = userController.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("Петя", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        verify(passwordEncoder, times(1)).encode("password");
        verify(roleRepository, times(1)).findByRoleName(RoleType.ROLE_USER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void me_AuthenticatedUser_ReturnsUserDto() {
        // Arrange
        Principal principal = () -> "testuser";
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDto result = userController.me(principal);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Петя", result.getFirstName());
        assertEquals("Иванов", result.getLastName());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void all_AdminUser_ReturnsAllUsers() {
        // Arrange
        User anotherUser = User.builder()
                .id(2L)
                .username("anotheruser")
                .password("encodedPassword")
                .firstName("Петя")
                .lastName("Иванов")
                .middleName("Петрович")
                .enabled(true)
                .userRoles(Set.of(userRole))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(testUser, anotherUser));

        // Act
        List<UserDto> result = userController.all();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_AdminUser_DeletesUser() {
        // Act
        userController.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }
}