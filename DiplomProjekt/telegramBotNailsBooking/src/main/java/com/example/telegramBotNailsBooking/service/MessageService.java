package com.example.telegramBotNailsBooking.service;

import com.example.telegramBotNailsBooking.bot.BotSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
public class MessageService {

    private final ObjectProvider<BotSender> botSenderProvider;

    @Autowired
    public MessageService(ObjectProvider<BotSender> botSenderProvider) {
        this.botSenderProvider = botSenderProvider;
    }

    public void sendMessage(Long chatId, String text) {
        BotSender botSender = botSenderProvider.getIfAvailable();
        if (botSender != null) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);
            botSender.sendMessage(chatId, message);
        } else {
            System.err.println("BotSender is not available");
        }
    }

    public void sendMessageWithInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
        BotSender botSender = botSenderProvider.getIfAvailable();
        if (botSender != null) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);
            message.setReplyMarkup(keyboardMarkup);
            botSender.sendMessage(chatId, message);
        } else {
            System.err.println("BotSender is not available");
        }
    }
}
