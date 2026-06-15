package com.junmo.certificatesystem.entitiy;

import com.junmo.certificatesystem.common.entity.BaseTimeEntity;
import com.junmo.certificatesystem.common.enums.RoleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;           // 로그인 ID

    @Column(nullable = false, length = 255)
    private String password;        // BCrypt 암호화

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;        // USER | ADMIN

    @Column(nullable = false)
    private boolean enabled = true;
}