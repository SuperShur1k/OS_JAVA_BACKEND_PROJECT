package com.example.telegramBotNailsBooking.service;

import com.example.telegramBotNailsBooking.bot.BotController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final BotController botController;

    public NotificationService(BotController botController) {
        this.botController = botController;
    }

    public void sendNotification(Long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(messageText);

        try {
            botController.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

