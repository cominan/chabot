package com.hung.chatbot.entity;


import jakarta.persistence.*;
import lombok.Data;
/**
 * @author Admin
 * @since 11/16/2025
 */
//@Entity
//@Data
//@Table(name = "users")
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long userId;
//
//    @Column(nullable = false, unique = true)
//    private String username;
//
//    private String email;
//
//    @Column(nullable = false, columnDefinition = "VARCHAR2(255) DEFAULT 'CHANGE_ME'")
//    private String password;
//
//
//    @Column(name = "provider")
//    private String provider;
//
//    @Column(name = "provider_id")
//    private String providerId;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Role role = Role.USER;  // Thêm dòng này
//
//    // Thêm enum Role
//    public enum Role {
//        USER,
//        ADMIN,
//        MODERATOR
//    }
//}

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'CHANGE_ME'")
    private String password;

    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    public enum Role {
        USER, ADMIN, MODERATOR
    }
}