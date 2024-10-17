package com.example.telegramBotNailsBooking.model;

import com.example.telegramBotNailsBooking.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    // Хранение информации о пользователе и их состоянии
    private Map<Long, String[]> userInfoMap = new HashMap<>(); // Для регистрации
    private Map<Long, String[]> userLoginInfoMap = new HashMap<>(); // Для логина
    private Map<Long, User> authenticatedUsers = new HashMap<>(); // Для аутентифицированных пользователей
    private Map<Long, Boolean> setAdminInProgress = new HashMap<>(); // Для отслеживания состояния назначения администраторов

    public String[] getUserInfo(Long chatId) {
        return userInfoMap.get(chatId);
    }

    public void setUserInfo(Long chatId, String[] userInfo) {
        userInfoMap.put(chatId, userInfo);
    }

    public void removeUserInfo(Long chatId) {
        userInfoMap.remove(chatId);
    }

    public String[] getUserLoginInfo(Long chatId) {
        return userLoginInfoMap.get(chatId);
    }

    public void setUserLoginInfo(Long chatId, String[] loginInfo) {
        userLoginInfoMap.put(chatId, loginInfo);
    }

    public void removeUserLoginInfo(Long chatId) {
        userLoginInfoMap.remove(chatId);
    }

    public void addAuthenticatedUser(Long chatId, User user) {
        authenticatedUsers.put(chatId, user);
    }

    public void removeAuthenticatedUser(Long chatId) {
        authenticatedUsers.remove(chatId);
    }

    public boolean isAuthenticated(Long chatId) {
        return authenticatedUsers.containsKey(chatId);
    }

    // Метод для установки состояния назначения администратора
    public void setSettingAdmin(Long chatId, boolean value) {
        setAdminInProgress.put(chatId, value);
    }

    public boolean isSettingAdmin(Long chatId) {
        return setAdminInProgress.getOrDefault(chatId, false);
    }

}
