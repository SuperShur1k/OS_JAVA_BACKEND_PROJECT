package com.example.telegramBotNailsBooking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)  // Минимум 2 символа, максимум 50 символов
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)  // Минимум 2 символа, максимум 50 символов
    private String lastName;

    @NotBlank
    @Size(min = 3, max = 50)  // Минимум 3 символа, максимум 50 символов
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(min = 6, max = 255)  // Минимум 6 символов, максимум 255 символов
    private String password;

    @NotNull
    @Pattern(regexp = "^(\\+\\d{1,3})?\\d{10}$", message = "Invalid phone number. Must start with + and followed by digits only.")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role; // Enum: CLIENT, ADMIN

    @Column(unique = true)
    private Long chatId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum Role {
        CLIENT,
        ADMIN,
        MASTER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
