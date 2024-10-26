package com.example.telegramBotNailsBooking.bot.commands;

import com.example.telegramBotNailsBooking.model.UserSession;
import com.example.telegramBotNailsBooking.service.MenuService;
import com.example.telegramBotNailsBooking.service.MessageService;
import com.example.telegramBotNailsBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthenticatedCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatedCommandHandler.class);

    @Autowired
    private UserSession userSession;

    @Autowired
    private MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    private MenuService menuService;

    public void handleAuthenticatedCommand(Long chatId, String text) {
        logger.info("Received command: {} for chat ID {}", text, chatId);

        switch (text) {
            case "/main_menu":
                mainMenu(chatId);
                break;
            case "/logout":
                logout(chatId);
                userSession.clearStates(chatId);
                break;
            case "/back":
                goBack(chatId);
                break;
            case "/menu":
                userSession.setCurrentState(chatId, "/menu");
                userSession.setPreviousState(chatId, "/main_menu");
                messageService.sendMessageWithInlineKeyboard(chatId, "Here is your menu:", menuService.getMenuInlineKeyboard());
                break;
            case "/settings":
                userSession.setCurrentState(chatId, "/settings");
                userSession.setPreviousState(chatId, "/menu");
                menuService.handleSettingsCommand(chatId, messageService);
                break;
            default:
                messageService.sendMessageWithInlineKeyboard(chatId, "Sorry, command was not recognized. Type /help for available commands.", getAuthenticatedInlineKeyboard(chatId));
                break;
        }
    }

    private void logout(Long chatId) {
        if (userSession.isAuthenticated(chatId)) {
            userSession.removeAuthenticatedUser(chatId);
            userSession.clearSession(chatId);
            userSession.clearStates(chatId);
            messageService.sendMessageWithInlineKeyboard(chatId, "You have been logged out successfully.", getUnauthenticatedInlineKeyboard());
        } else {
            messageService.sendMessageWithInlineKeyboard(chatId, "You are not logged in.", getUnauthenticatedInlineKeyboard());
        }
    }

    public void goBack(Long chatId) {
        // Получаем предыдущее состояние пользователя
        String previousState = userSession.getPreviousState(chatId);
        logger.info("Attempting to go back to previous state: {} for chat ID {}", previousState, chatId);

        if (previousState != null) {
            // Переходим к предыдущему состоянию
            handleAuthenticatedCommand(chatId, previousState);
        } else {
            // Если предыдущее состояние отсутствует, возвращаем пользователя в главное меню
            messageService.sendMessageWithInlineKeyboard(chatId, "No previous state found. Returning to the main menu.", getAuthenticatedInlineKeyboard(chatId));
        }
    }

    private void mainMenu(Long chatId) {
        userSession.clearStates(chatId);
        messageService.sendMessageWithInlineKeyboard(chatId, "Main Menu", getAuthenticatedInlineKeyboard(chatId));
    }


    private InlineKeyboardMarkup getAuthenticatedInlineKeyboard(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton restrictedButton = new InlineKeyboardButton();
        restrictedButton.setText("Menu");
        restrictedButton.setCallbackData("/menu");
        row1.add(restrictedButton);

        InlineKeyboardButton logoutButton = new InlineKeyboardButton();
        logoutButton.setText("Logout");
        logoutButton.setCallbackData("/logout");
        row1.add(logoutButton);

        if (userService.isAdmin(chatId)){
            InlineKeyboardButton adminPanelButton = new InlineKeyboardButton();
            adminPanelButton.setText("Admin Panel");
            adminPanelButton.setCallbackData("/admin");
            row2.add(adminPanelButton);
        }


        rowsInline.add(row1);
        rowsInline.add(row2);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getUnauthenticatedInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton registerButton = new InlineKeyboardButton();
        registerButton.setText("Register");
        registerButton.setCallbackData("/register");
        row1.add(registerButton);

        InlineKeyboardButton loginButton = new InlineKeyboardButton();
        loginButton.setText("Login");
        loginButton.setCallbackData("/login");
        row1.add(loginButton);

        InlineKeyboardButton helpButton = new InlineKeyboardButton();
        helpButton.setText("Help");
        helpButton.setCallbackData("/help");
        row2.add(helpButton);

        rowsInline.add(row1);
        rowsInline.add(row2);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }

}
