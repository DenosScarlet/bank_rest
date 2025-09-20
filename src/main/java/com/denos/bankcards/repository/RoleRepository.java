package com.denos.bankcards.repository;

import com.denos.bankcards.entity.Role;
import com.denos.bankcards.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleType roleName);
}
