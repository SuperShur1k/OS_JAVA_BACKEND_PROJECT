package com.example.telegramBotNailsBooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppoimentDTO {
    private Long id;
    private Long masterId;
    private LocalDateTime dateTime;
    private String serviceName;
}
