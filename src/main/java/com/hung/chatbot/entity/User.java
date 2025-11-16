package com.hung.chatbot.entity;


import jakarta.persistence.*;
import lombok.Data;
/**
 * @author Admin
 * @since 11/16/2025
 */
@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;
}