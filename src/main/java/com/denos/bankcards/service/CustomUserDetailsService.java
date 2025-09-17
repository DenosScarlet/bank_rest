package com.denos.bankcards.service;

import com.denos.bankcards.entity.User;
import com.denos.bankcards.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var authorities = u.getUserRoles().stream().map(r -> new SimpleGrantedAuthority(r.getRoleName().name())).toList();
        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .authorities(authorities)
                .disabled(!u.isEnabled())
                .build();
    }
}
