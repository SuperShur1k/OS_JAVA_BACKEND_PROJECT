package com.example.telegramBotNailsBooking.bot.commands;

import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.model.UserSession;
import com.example.telegramBotNailsBooking.repository.UserRepository;
import com.example.telegramBotNailsBooking.service.MessageService;
import com.example.telegramBotNailsBooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class UnauthenticatedCommandHandler {

    @Autowired
    private UserSession userSession;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    public void handleUnauthenticatedCommand(Long chatId, String text) {
        String[] loginInfo = userSession.getUserLoginInfo(chatId);

        if (loginInfo != null) {
            handleLoginInput(chatId, text);
            return;
        }

        switch (text) {
            case "/start":
                startCommand(chatId);
                break;
            case "/help":
                messageService.sendMessageWithInlineKeyboard(chatId, "Here are the commands you can use:\n/register - Register a new user\n/login - Log in to your account", getUnauthenticatedInlineKeyboard());
                break;
            case "/register":
                if (userRepository.findByChatId(chatId) != null) {
                    messageService.sendMessageWithInlineKeyboard(chatId, "You are already registered.", getUnauthenticatedInlineKeyboard());
                } else {
                    initiateRegistration(chatId);
                }
                break;
            case "/login":
                initiateLogin(chatId);
                break;
            case "/cancel":
                cancel(chatId);
                break;
            default:
                if (text.startsWith("/")) {
                    messageService.sendMessageWithInlineKeyboard(chatId, "Sorry, command was not recognized. Type /help for available commands.", getUnauthenticatedInlineKeyboard());
                }
                break;
        }
    }

    private void startCommand(Long chatId) {
        messageService.sendMessageWithInlineKeyboard(chatId, "Welcome! This bot can help you with registration, login, and more. Type /help for a list of commands.", getUnauthenticatedInlineKeyboard());
    }

    private void initiateRegistration(Long chatId) {
        messageService.sendMessage(chatId, "Please enter your first name to begin registration.");
        userSession.setUserInfo(chatId, new String[5]);
        messageService.sendMessageWithInlineKeyboard(chatId, "You can cancel the registration at any time using the button below.", getCancelInlineKeyboard());
    }

    private void initiateLogin(Long chatId) {
        messageService.sendMessage(chatId, "Please enter your username.");
        userSession.setUserLoginInfo(chatId, new String[2]);
        messageService.sendMessageWithInlineKeyboard(chatId, "You can cancel the login at any time using the button below.", getCancelInlineKeyboard());
    }


    private void handleLoginInput(Long chatId, String text) {
        String[] loginInfo = userSession.getUserLoginInfo(chatId);
        if (loginInfo[0] == null) {
            loginInfo[0] = text; // Сохраняем введенный логин
            userSession.setUserLoginInfo(chatId, loginInfo);
            messageService.sendMessage(chatId, "Please enter your password.");
        } else if (loginInfo[1] == null) {
            loginInfo[1] = text; // Сохраняем введенный пароль
            userSession.setUserLoginInfo(chatId, loginInfo);

            // Проверяем данные
            User user = userRepository.findByUsername(loginInfo[0]);
            if (user == null) {
                messageService.sendMessage(chatId, "Username not found. Please try again or register.");
                userSession.clearSession(chatId); // Очистка сессии при неудачной попытке
            } else if (!passwordEncoder.matches(text, user.getPassword())) {
                messageService.sendMessage(chatId, "Incorrect password. Please try again.");
                userSession.clearSession(chatId);
            } else {
                messageService.sendMessageWithInlineKeyboard(chatId, "Login successful! Welcome back, " + user.getFirstName() + ".", getAuthenticatedInlineKeyboard(chatId));
                userSession.authenticateUser(chatId); // Устанавливаем, что пользователь аутентифицирован
            }
        }
    }

    private void cancel(Long chatId) {
        userSession.clearSession(chatId);
        messageService.sendMessageWithInlineKeyboard(chatId, "Current operation has been cancelled.", getUnauthenticatedInlineKeyboard());
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

    private InlineKeyboardMarkup getCancelInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Cancel");
        cancelButton.setCallbackData("/cancel");
        row1.add(cancelButton);

        rowsInline.add(row1);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }

}
