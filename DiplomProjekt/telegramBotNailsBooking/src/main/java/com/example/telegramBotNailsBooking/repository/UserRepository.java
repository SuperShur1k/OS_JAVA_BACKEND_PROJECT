package com.example.telegramBotNailsBooking.repository;


import com.example.telegramBotNailsBooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByChatId(Long chatId);
    User findByPhoneNumber(String phoneNumber);
}
