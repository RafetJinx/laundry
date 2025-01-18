package com.laundry.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private String displayName;

    @Column(nullable = false)
    private String password;

    private String email;
    private String phone;
    private String address;

    @Column(nullable = false)
    private String role;

    private String resetToken;

    @Column(name="reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Order> orders = new ArrayList<>();
}
