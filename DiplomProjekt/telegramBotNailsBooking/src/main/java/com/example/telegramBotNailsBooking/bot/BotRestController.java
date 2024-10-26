package com.example.telegramBotNailsBooking.bot;

import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bot")
public class BotRestController {

    @Autowired
    private UserService userService;

    @PostMapping("/command")
    public String handleCommand(@RequestParam Long chatId, @RequestParam String command) {
        // Здесь идет обращение к `commandController` для обработки команды, если нужно
        return "Command processed";
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(404).body("User not found");
        }
    }
}
