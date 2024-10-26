package com.example.telegramBotNailsBooking.bot.commands;

import com.example.telegramBotNailsBooking.model.UserSession;
import com.example.telegramBotNailsBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommandController {

    @Autowired
    private UnauthenticatedCommandHandler unauthenticatedCommandHandler;

    @Autowired
    private AuthenticatedCommandHandler authenticatedCommandHandler;

    @Autowired
    private AdminCommandHandler adminCommandHandler;

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserService userService;

    public void handleCommand(Long chatId, String command) {
        // Обработка общей команды отмены
        if (command.equals("/cancel")) {
            if (userSession.isAuthenticated(chatId) && userService.isAdmin(chatId)) {
                userSession.clearSession(chatId);
                adminCommandHandler.handleAdminCommand(chatId, "/admin");
            } else if (userSession.isAuthenticated(chatId)) {
                authenticatedCommandHandler.goBack(chatId);
            } else {
                userSession.clearSession(chatId);
                unauthenticatedCommandHandler.handleUnauthenticatedCommand(chatId, "/cancel");
            }
            return;
        }

        // Определение статуса пользователя и выбор нужного обработчика
        if (userSession.isAuthenticated(chatId)) {
            if (userService.isAdmin(chatId)) {
                adminCommandHandler.handleAdminCommand(chatId, command);
            } else {
                authenticatedCommandHandler.handleAuthenticatedCommand(chatId, command);
            }
        } else {
            unauthenticatedCommandHandler.handleUnauthenticatedCommand(chatId, command);
        }
    }
}
