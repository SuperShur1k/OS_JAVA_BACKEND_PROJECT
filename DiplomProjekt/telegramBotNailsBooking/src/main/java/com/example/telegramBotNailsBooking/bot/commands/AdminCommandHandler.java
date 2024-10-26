package com.example.telegramBotNailsBooking.bot.commands;

import com.example.telegramBotNailsBooking.controller.AdminController;
import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.model.UserSession;
import com.example.telegramBotNailsBooking.repository.UserRepository;
import com.example.telegramBotNailsBooking.service.AdminService;
import com.example.telegramBotNailsBooking.service.MenuService;
import com.example.telegramBotNailsBooking.service.MessageService;
import com.example.telegramBotNailsBooking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminCommandHandler extends AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCommandHandler.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSession userSession;

    @Autowired
    private MessageService messageService;

    public void handleAdminCommand(Long chatId, String text) {
        logger.info("Received command: {} for chat ID {}", text, chatId);

        if (userSession.isSettingAdmin(chatId)) {
            setAdmin(chatId, text);
            return;
        }

        if (userSession.isSettingMaster(chatId)) {
            addMaster(chatId, text);
            return;
        }

        switch (text) {
            case "/main_menu":
                mainMenu(chatId);
                break;
            case "/admin":
                userSession.setCurrentState(chatId, "/admin");
                userSession.setPreviousState(chatId, "/main_menu");
                showAdminPanel(chatId);
                break;
            case "/setadmin":
                initiateSetAdmin(chatId);
                break;
            case "/back":
                goBack(chatId);
                break;
            case "/cancel":
                cancel(chatId);
                break;
            case "/logout":
                logout(chatId);
                userSession.clearStates(chatId);
                break;
            case "/add_master":
                initiateAddMaster(chatId);
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
                messageService.sendMessageWithInlineKeyboard(chatId, "Sorry, command was not recognized. Type /help for available commands.", getAdminInlineKeyboard());
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

    private void mainMenu(Long chatId) {
        userSession.clearStates(chatId);
        messageService.sendMessageWithInlineKeyboard(chatId, "Main Menu", getAuthenticatedInlineKeyboard(chatId));
    }

    private void goBack(Long chatId) {
        // Получаем предыдущее состояние пользователя
        String previousState = userSession.getPreviousState(chatId);
        logger.info("Attempting to go back to previous state: {} for chat ID {}", previousState, chatId);

        if (previousState != null) {
            // Переходим к предыдущему состоянию
            handleAdminCommand(chatId, previousState);
        } else {
            // Если предыдущее состояние отсутствует, возвращаем пользователя в главное меню
            messageService.sendMessageWithInlineKeyboard(chatId, "No previous state found. Returning to the main menu.", getAuthenticatedInlineKeyboard(chatId));
        }
    }

    private void cancel(Long chatId) {
        userSession.clearSession(chatId);
        messageService.sendMessageWithInlineKeyboard(chatId, "Current operation has been cancelled.", getAdminInlineKeyboard());
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

        if (userService.isAdmin(chatId)) {
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
}
