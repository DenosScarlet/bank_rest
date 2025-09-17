package com.denos.bankcards.entity;

import com.denos.bankcards.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false)
    private Long id;
    @Column(name = "role_name",unique = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private RoleType roleName;
}
