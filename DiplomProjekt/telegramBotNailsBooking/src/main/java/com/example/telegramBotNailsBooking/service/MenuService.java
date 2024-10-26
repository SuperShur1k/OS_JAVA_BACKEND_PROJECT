package com.example.telegramBotNailsBooking.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {

    // Метод для создания общего меню
    public InlineKeyboardMarkup getMenuInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Кнопка настроек
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton bookingButton = new InlineKeyboardButton();

        InlineKeyboardButton settingsButton = new InlineKeyboardButton();
        settingsButton.setText("Settings");
        settingsButton.setCallbackData("/settings");
        row1.add(settingsButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("Back");
        backButton.setCallbackData("/back");
        row2.add(backButton);

        InlineKeyboardButton logoutButton = new InlineKeyboardButton();
        logoutButton.setText("Logout");
        logoutButton.setCallbackData("/logout");
        row2.add(logoutButton);

        rowsInline.add(row1);
        rowsInline.add(row2);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }

    // Метод для обработки команды /settings
    public void handleSettingsCommand(Long chatId, MessageService messageService) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        // Создание кнопки "Back"
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText("Back");
        backButton.setCallbackData("/back");
        row1.add(backButton);

        rowsInline.add(row1);
        inlineKeyboardMarkup.setKeyboard(rowsInline);

        // Отправка сообщения с клавиатурой
        messageService.sendMessageWithInlineKeyboard(chatId, "Here you can change your settings.", inlineKeyboardMarkup);
    }
}
