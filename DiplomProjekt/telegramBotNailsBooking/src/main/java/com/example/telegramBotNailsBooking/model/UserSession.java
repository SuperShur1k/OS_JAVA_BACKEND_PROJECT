package com.example.telegramBotNailsBooking.model;

import com.example.telegramBotNailsBooking.model.User;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserSession {
    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);

    private Map<Long, String[]> userLoginInfo = new HashMap<>();
    private Map<Long, String[]> userInfo = new HashMap<>();
    private Map<Long, Boolean> authenticatedUsers = new HashMap<>();
    private Map<Long, Boolean> settingAdmin = new HashMap<>();
    private Map<Long, String> userStates = new HashMap<>();
    private Map<Long, String> previousStates = new HashMap<>();
    private Map<Long, Boolean> settingMaster = new HashMap<>();

    public void setUserLoginInfo(Long chatId, String[] info) {
        userLoginInfo.put(chatId, info);
    }

    public String[] getUserLoginInfo(Long chatId) {
        return userLoginInfo.get(chatId);
    }

    public void removeUserLoginInfo(Long chatId) {
        userLoginInfo.remove(chatId);
    }

    public void setUserInfo(Long chatId, String[] info) {
        userInfo.put(chatId, info);
    }

    public String[] getUserInfo(Long chatId) {
        return userInfo.get(chatId);
    }

    public void removeUserInfo(Long chatId) {
        userInfo.remove(chatId);
    }

    public void authenticateUser(Long chatId) {
        authenticatedUsers.put(chatId, true);
    }

    public void removeAuthenticatedUser(Long chatId) {
        authenticatedUsers.remove(chatId);
    }

    public boolean isAuthenticated(Long chatId) {
        return authenticatedUsers.getOrDefault(chatId, false);
    }

    public void setSettingAdmin(Long chatId, boolean isSetting) {
        settingAdmin.put(chatId, isSetting);
    }

    public boolean isSettingAdmin(Long chatId) {
        return settingAdmin.getOrDefault(chatId, false);
    }

    public void setSettingMaster(Long chatId, boolean isSetting) {
        settingMaster.put(chatId, isSetting);
    }

    public boolean isSettingMaster(Long chatId) {
        return settingMaster.getOrDefault(chatId, false);
    }

    public void clearSession(Long chatId) {
        userLoginInfo.remove(chatId);
        userInfo.remove(chatId);
        settingAdmin.remove(chatId); // Удаляем только настройки администратора
        settingMaster.remove(chatId);
    }

    public void setCurrentState(Long chatId, String currentState) {
        // Сохраняем текущее состояние как предыдущее
        if (userStates.containsKey(chatId)) {
            previousStates.put(chatId, userStates.get(chatId));
        }
        logger.info("Setting current state for chat ID {}: {}", chatId, currentState);
        userStates.put(chatId, currentState); // Устанавливаем новое текущее состояние
    }

    public String getCurrentState(Long chatId) {
        String state = userStates.get(chatId);
        logger.info("Getting current state for chat ID {}: {}", chatId, state);
        return state;  // Возвращаем текущее состояние
    }

    public String getPreviousState(Long chatId) {
        String state = previousStates.get(chatId);
        logger.info("Getting previous state for chat ID {}: {}", chatId, state);
        return state;  // Возвращаем предыдущее состояние
    }

    public void setPreviousState(Long chatId, String previousState) {
        logger.info("Setting previous state for chat ID {}: {}", chatId, previousState);
        previousStates.put(chatId, previousState);  // Устанавливаем предыдущее состояние
    }

    public void clearStates(Long chatId) {
        logger.info("Clearing states for chat ID {}", chatId);
        userStates.remove(chatId);
        previousStates.remove(chatId);
    }
}

