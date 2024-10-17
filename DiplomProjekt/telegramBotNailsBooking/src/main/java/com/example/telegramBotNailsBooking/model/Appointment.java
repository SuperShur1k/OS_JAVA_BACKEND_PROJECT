package com.example.telegramBotNailsBooking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "master_id", nullable = false)
    private Master master;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    private Status status; // Enum: CONFIRMED, CANCELLED, COMPLETED

    // Enum для статусов записи
    public enum Status {
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}