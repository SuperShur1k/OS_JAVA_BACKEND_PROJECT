package com.example.telegramBotNailsBooking.service;

import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String firstName, String lastName, String username, String rawPassword, Long chatId, String phoneNumber) {
        // Проверка на существование пользователя по уникальным полям
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists!"); // Возвращаем ошибку, если имя пользователя уже существует
        }
        if (userRepository.findByChatId(chatId) != null) {
            throw new IllegalArgumentException("User with this chat ID already exists!"); // Возвращаем ошибку, если чат ID уже существует
        }
        if (userRepository.findByPhoneNumber(phoneNumber) != null) {
            throw new IllegalArgumentException("User with this phone number already exists!"); // Возвращаем ошибку, если телефонный номер уже существует
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword)); // Хешируем пароль
        user.setChatId(chatId);
        user.setPhoneNumber(phoneNumber);
        user.setRole(User.Role.CLIENT); // Задаем роль по умолчанию

        log.info("Registering new user: {}", username);
        return userRepository.save(user); // Сохраняем пользователя
    }


    public User loginUser(String username, String rawPassword) {
        // Ищем пользователя по имени
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("Login failed: user not found with username {}", username);
            return null;
        }

        // Сравниваем введенный пароль с захешированным
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.error("Login failed: incorrect password for username {}", username);
            return null;
        }

        // Обновляем поле updatedAt при успешном логине
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user); // Сохраняем изменения

        log.info("Login successful for user: {}", username);
        return user;
    }
}


