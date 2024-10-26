package com.example.telegramBotNailsBooking.controller;

import com.example.telegramBotNailsBooking.dto.UserDTO;
import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            userService.registerUser(
                    userDTO.getFirstName(),
                    userDTO.getLastName(),
                    userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getPhoneNumber()
            );
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO userDTO) {
        User user = userService.loginUser(userDTO.getUsername(), userDTO.getPassword());
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        userService.authenticateUser(user.getChatId());
        return ResponseEntity.ok("User logged in successfully");
    }
}
