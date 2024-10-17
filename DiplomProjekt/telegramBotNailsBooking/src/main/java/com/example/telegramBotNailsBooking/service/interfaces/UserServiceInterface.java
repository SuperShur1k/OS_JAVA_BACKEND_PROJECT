package com.example.telegramBotNailsBooking.service.interfaces;

import com.example.telegramBotNailsBooking.model.User;

public interface UserServiceInterface {
    void registerUser(User user);
    User findUserByUsername(String username);
}

