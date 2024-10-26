package com.example.telegramBotNailsBooking.controller;

import com.example.telegramBotNailsBooking.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/setadmin")
    public String setAdmin(@RequestParam Long chatId, @RequestParam String username) {
        adminService.setAdmin(chatId, username);
        return "Admin rights granted to " + username;
    }

    @PostMapping("/cancel")
    public String cancelAdminAction(@RequestParam Long chatId) {
        adminService.cancelAdminAction(chatId);
        return "Admin action cancelled.";
    }

    @GetMapping("/panel")
    public String showAdminPanel(@RequestParam Long chatId) {
        adminService.showAdminPanel(chatId);
        return "Admin panel shown.";
    }
}
