package com.example.telegramBotNailsBooking.service;

import com.example.telegramBotNailsBooking.bot.commands.AdminCommandHandler;
import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.repository.UserRepository;
import com.example.telegramBotNailsBooking.model.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminCommandHandler.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserSession userSession;

    public void showAdminPanel(Long chatId) {
        messageService.sendMessageWithInlineKeyboard(chatId, "Welcome to the Admin Panel. You can manage users and set new admins.", getAdminInlineKeyboard());
        userSession.clearStates(chatId);
        userSession.setCurrentState(chatId, "/admin");
        userSession.setPreviousState(chatId, "/main_menu");
    }

    protected void initiateAddMaster(Long chatId) {
        messageService.sendMessage(chatId, "Please enter the username of the user you want to make an master.");
        userSession.setSettingMaster(chatId, true);
        messageService.sendMessageWithInlineKeyboard(chatId, "You can cancel this operation using the button below.", getCancelInlineKeyboard());
    }

    public void addMaster(Long chatId, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            messageService.sendMessageWithInlineKeyboard(chatId, "User not found. Please try again.", getAdminInlineKeyboard());
            userSession.setSettingMaster(chatId, false); // Сбрасываем состояние назначения мастера
        } else {
            user.setRole(User.Role.MASTER);

            log.info("Saving user with role: {}", user.getRole());

            userRepository.save(user);
            messageService.sendMessageWithInlineKeyboard(chatId, "User " + username + " has been successfully added as a master.", getAdminInlineKeyboard());
            userSession.setSettingMaster(chatId, false); // Сбрасываем состояние назначения мастера
        }
    }

    protected void initiateSetAdmin(Long chatId) {
        messageService.sendMessage(chatId, "Please enter the username of the user you want to make an admin.");
        userSession.setSettingAdmin(chatId, true);
        messageService.sendMessageWithInlineKeyboard(chatId, "You can cancel this operation using the button below.", getCancelInlineKeyboard());
    }

    public void setAdmin(Long chatId, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            messageService.sendMessageWithInlineKeyboard(chatId, "User not found. Please try again.", getAdminInlineKeyboard());
            userSession.setSettingAdmin(chatId, false); // Сбрасываем состояние назначения администратора
        } else {
            user.setRole(User.Role.ADMIN);

            log.info("Saving user with role: {}", user.getRole());

            userRepository.save(user);
            messageService.sendMessageWithInlineKeyboard(chatId, "User " + username + " has been successfully granted admin rights.", getAdminInlineKeyboard());
            userSession.setSettingAdmin(chatId, false); // Сбрасываем состояние назначения администратора
        }
    }
    public void cancelAdminAction(Long chatId) {
        userSession.clearSession(chatId);
        messageService.sendMessageWithInlineKeyboard(chatId, "Current operation has been cancelled.", getAdminInlineKeyboard());
    }

    protected InlineKeyboardMarkup getAdminInlineKeyboard() {
        // Код для создания клавиатуры администратора
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton setAdminButton = new InlineKeyboardButton();
        setAdminButton.setText("Set Admin");
        setAdminButton.setCallbackData("/setadmin");
        row1.add(setAdminButton);
        InlineKeyboardButton setMasterButton = new InlineKeyboardButton();
        setMasterButton.setText("Set Master");
        setMasterButton.setCallbackData("/add_master");
        row1.add(setMasterButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton logoutButton = new InlineKeyboardButton();
        logoutButton.setText("Logout");
        logoutButton.setCallbackData("/logout");
        row2.add(logoutButton);

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("Back");
        backButton.setCallbackData("/back");
        row2.add(backButton);

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
