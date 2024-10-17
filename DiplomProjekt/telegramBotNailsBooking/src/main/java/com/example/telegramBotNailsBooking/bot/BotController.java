package com.example.telegramBotNailsBooking.bot;

import com.example.telegramBotNailsBooking.model.User;
import com.example.telegramBotNailsBooking.model.UserSession;
import com.example.telegramBotNailsBooking.repository.UserRepository;
import com.example.telegramBotNailsBooking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotController extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(BotController.class);

    private final BotConfig config;
    private final UserService userService;
    private final UserRepository userRepository;

    private UserSession userSession = new UserSession(); // Используем класс UserSession

    @Autowired
    public BotController(BotConfig config, UserService userService, UserRepository userRepository) {
        this.config = config;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            logger.info("Received message from chat ID: {} with text: {}", chatId, text);
            handleCommand(chatId, text);
        } else if (update.hasCallbackQuery()) {
            // Обрабатываем callback query, когда пользователь нажимает на кнопку
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            logger.info("Received callback query from chat ID: {} with data: {}", chatId, callbackData);
            handleCommand(chatId, callbackData);  // Обрабатываем callback data как команду
        } else {
            logger.warn("Received an update that is not a message or does not contain text.");
        }
    }

    private void handleCommand(Long chatId, String text) {
        if (text.equals("/cancel")) {
            cancelRegistration(chatId);
            return;
        }

        // Проверяем состояние назначения администратора
        if (userSession.isSettingAdmin(chatId)) {
            setAdmin(chatId, text);
            return;
        }

        // Проверяем состояние логина
        String[] loginInfo = userSession.getUserLoginInfo(chatId);
        if (loginInfo != null) {
            handleLoginInput(chatId, text);
            return;
        }

        // Проверяем состояние регистрации
        String[] userInfo = userSession.getUserInfo(chatId);
        if (userInfo != null) {
            handleRegistrationInput(chatId, text);
            return;
        }

        // Обработка команд
        switch (text) {
            case "/start":
                startCommand(chatId);
                break;
            case "/help":
                sendMessage(chatId, "Here are the commands you can use:\n/start - Start the bot" +
                        "\n/help - Get help\n/register - Register a new user" +
                        "\n/login - Log in to your account" +
                        "\n/admin - Admin area" +
                        "\n/setadmin - Grant admin rights to a user" +
                        "\n/logout - Logout the bot");
                break;
            case "/register":
                // Проверяем, есть ли пользователь с таким chatId в базе данных
                if (userRepository.findByChatId(chatId) != null) {
                    sendMessage(chatId, "You are already registered."); // Если пользователь уже зарегистрирован
                } else {
                    initiateRegistration(chatId); // Иначе начинаем процесс регистрации
                }
                break;
            case "/login":
                initiateLogin(chatId);
                break;
            case "/restricted":
                if (userSession.isAuthenticated(chatId)) {
                    sendMessage(chatId, "This is restricted content, available only for authenticated users.");
                } else {
                    sendMessage(chatId, "Please log in to access this functionality.");
                }
                break;
            case "/admin":
                if (isAdmin(chatId) && userSession.isAuthenticated(chatId)) {
                    sendMessage(chatId, "Welcome, admin! You can manage users or view logs.");
                } else if (!userSession.isAuthenticated(chatId)) {
                    sendMessage(chatId, "Please log in to access this functionality.");
                } else {
                    sendMessage(chatId, "Access denied. Admins only.");
                }
                break;
            case "/setadmin":
                if (isAdmin(chatId) && userSession.isAuthenticated(chatId)) {
                    sendMessage(chatId, "Enter the username of the user you want to promote to admin:");
                    userSession.setSettingAdmin(chatId, true); // Устанавливаем состояние
                } else if (!userSession.isAuthenticated(chatId)) {
                    sendMessage(chatId, "Please log in to access this functionality.");
                } else {
                    sendMessage(chatId, "Access denied. Admins only.");
                }
                break;
            case "/logout":
                logout(chatId);
                break;
            default:
                if (text.contains("/")) {
                sendMessage(chatId, "Sorry, command was not recognized. Type /help for available commands.");}
                break;
        }
    }

    private void logout(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Register").callbackData("/register").build());
        row1.add(InlineKeyboardButton.builder().text("Login").callbackData("/login").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Help").callbackData("/help").build());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        markup.setKeyboard(keyboard);

        String logout;

        if (userSession.isAuthenticated(chatId)) {
            userSession.removeAuthenticatedUser(chatId);
            logout = "You have been logged out successfully.";
        } else {
            logout = "You are not logged in.";
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId)); // Устанавливаем ID чата
        message.setText(logout); // Устанавливаем текст сообщения
        message.setReplyMarkup(markup); // Устанавливаем клавиатуру

        // Отправляем сообщение
        sendMessage(chatId, message);
    }

    private void setAdmin(Long chatId, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            sendMessage(chatId, "User not found.");
        } else {
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
            sendMessage(chatId, "User " + username + " has been granted admin rights successfully.");
        }
    }

    private boolean isAdmin(Long chatId) {
        // Ищем пользователя по chatId и проверяем его роль
        User user = userRepository.findByChatId(chatId);
        return user != null && user.getRole() == User.Role.ADMIN; // Проверка на наличие пользователя и его роль
    }


    private void initiateLogin(Long chatId) {
        userSession.setUserLoginInfo(chatId, new String[2]); // Создаем новый массив для логина
        sendMessage(chatId, "Please enter your username.");
    }

    private void handleLoginInput(Long chatId, String text) {
        String[] loginInfo = userSession.getUserLoginInfo(chatId);
        if (loginInfo == null) {
            sendMessage(chatId, "You need to start the login process first by sending /login.");
            return;
        }

        if (loginInfo[0] == null) {
            User user = userRepository.findByUsername(text);
            if (user == null) {
                sendMessage(chatId, "Username is not correct. Please enter your username again.");
                 // Сбрасываем процесс логина
            } else {
                loginInfo[0] = text; // Сохраняем корректное имя пользователя
                sendMessage(chatId, "Please enter your password.");
            }
        } else if (loginInfo[1] == null) {
            loginInfo[1] = text; // Сохраняем пароль
            loginUser(chatId, loginInfo); // Пытаемся выполнить вход
        }
    }

    private void loginUser(Long chatId, String[] loginInfo) {
        // Создаем объект сообщения
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        String username = loginInfo[0];
        String password = loginInfo[1];

        // Вызываем метод loginUser из UserService для проверки пользователя
        User user = userService.loginUser(username, password);

        // Переменная для текста сообщения
        String loginMessage;

        if (user == null) {
            // Если вход не удался
            loginMessage = "Login failed. Please check your username and password and try again.";
            userSession.removeUserLoginInfo(chatId); // Очищаем процесс логина

            // Создаем клавиатуру для неудачного входа
            InlineKeyboardMarkup retryMarkup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(InlineKeyboardButton.builder().text("Register").callbackData("/register").build());
            row1.add(InlineKeyboardButton.builder().text("Login").callbackData("/login").build());

            List<InlineKeyboardButton> row2 = new ArrayList<>();
            row2.add(InlineKeyboardButton.builder().text("Help").callbackData("/help").build());

            List<List<InlineKeyboardButton>> retryKeyboard = new ArrayList<>();
            retryKeyboard.add(row1);
            retryKeyboard.add(row2);
            retryMarkup.setKeyboard(retryKeyboard);

            message.setReplyMarkup(retryMarkup); // Устанавливаем клавиатуру для неудачного входа
        } else {
            // Если вход успешен
            userSession.addAuthenticatedUser(chatId, user); // Добавляем в аутентифицированные пользователи
            loginMessage = "Login successful! Welcome, " + user.getFirstName() + "!";
            userSession.removeUserLoginInfo(chatId); // Удаляем данные после успешного логина

            // Создаем клавиатуру для успешного входа
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            row1.add(InlineKeyboardButton.builder().text("Logout").callbackData("/logout").build());

            if (isAdmin(chatId)) {
                row1.add(InlineKeyboardButton.builder().text("Add Admin").callbackData("/setadmin").build());
            }

            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            keyboard.add(row1);
            markup.setKeyboard(keyboard);
            message.setReplyMarkup(markup); // Устанавливаем клавиатуру для успешного входа
        }

        message.setText(loginMessage); // Устанавливаем текст сообщения

        // Отправляем сообщение
        sendMessage(chatId, message);
    }



    private void startCommand(Long chatId) {
        // Получаем пользователя из базы данных
        User user = userRepository.findByChatId(chatId); // Используйте метод findByChatId

        String welcomeMessage;
        if (user != null) {
            // Если пользователь найден, используем его имя
            welcomeMessage = "Hello " + user.getFirstName() + "! Welcome to the Nail Booking Bot! Choose an option below:";
        } else {
            // Если пользователь не найден (возможно, новый пользователь), используем стандартное приветствие
            welcomeMessage = "Hello! Welcome to the Nail Booking Bot! Choose an option below:";
        }

        // Создаем клавиатуру
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Register").callbackData("/register").build());
        row1.add(InlineKeyboardButton.builder().text("Login").callbackData("/login").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Help").callbackData("/help").build());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        markup.setKeyboard(keyboard);

        // Отправляем сообщение с кнопками
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeMessage); // Используем приветственное сообщение с именем пользователя
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error occurred while sending message to chat ID: {}", chatId, e);
        }
    }


    private void initiateRegistration(Long chatId) {
        // Создаем клавиатуру с кнопкой "Cancel"
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Cancel").callbackData("/cancel").build());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        markup.setKeyboard(keyboard); // Устанавливаем клавиатуру

        SendMessage message = new SendMessage(); // Создаем объект сообщения
        message.setChatId(String.valueOf(chatId)); // Устанавливаем ID чата

        // Инициализируем массив для хранения данных пользователя
        userSession.setUserInfo(chatId, new String[5]);

        // Устанавливаем текст сообщения
        String info = userSession.getUserInfo(chatId) != null
                ? "You are already in the process of registration. Press the button below to cancel the current registration."
                : ""; // Если не в процессе регистрации, ничего не добавляем

        String phone = "Please enter your phone number (e.g., +380123456789):\n" +
                "Phone number must start with '+', followed by 10 digits.";
        message.setText(info + (info.isEmpty() ? "" : "\n\n") + phone); // Устанавливаем текст сообщения

        // Отправляем сообщение
        message.setReplyMarkup(markup); // Устанавливаем клавиатуру
        sendMessage(chatId, message);
    }



    private void cancelRegistration(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Register").callbackData("/register").build());
        row1.add(InlineKeyboardButton.builder().text("Login").callbackData("/login").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Help").callbackData("/help").build());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        markup.setKeyboard(keyboard);

        String cancel;
        if (userSession.getUserInfo(chatId) != null) {
            userSession.removeUserInfo(chatId); // Удаляем данные из сессии
            cancel = "Your registration has been canceled.";
        } else {
            cancel = "You are not currently in the process of registration.";
        }

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId)); // Устанавливаем ID чата
        message.setText(cancel); // Устанавливаем текст сообщения
        message.setReplyMarkup(markup); // Устанавливаем клавиатуру

        // Отправляем сообщение
        sendMessage(chatId, message);
    }


    private void handleRegistrationInput(Long chatId, String input) {
        String[] userInfo = userSession.getUserInfo(chatId);

        if (userInfo == null) {
            sendMessage(chatId, "You need to start the registration process first by sending /register.");
            return;
        }

        if (userInfo[0] == null) {
            if (!input.matches("^\\+\\d{12,15}$")) { // Проверка на номер телефона
                sendMessage(chatId, "Please enter a valid phone number. Phone number must start with '+', followed by 10-15 digits.");
            } else {
                userInfo[0] = input; // Номер телефона
                sendMessage(chatId, "Please enter your first name (2-50 characters):");
            }
        } else if (userInfo[1] == null) {
            if (input.length() < 2 || input.length() > 50) {
                sendMessage(chatId, "Please enter a valid first name (2-50 characters):");
            } else {
                userInfo[1] = input; // Имя
                sendMessage(chatId, "Please enter your last name (2-50 characters):");
            }
        } else if (userInfo[2] == null) {
            if (input.length() < 2 || input.length() > 50) {
                sendMessage(chatId, "Please enter a valid last name (2-50 characters):");
            } else {
                userInfo[2] = input; // Фамилия
                sendMessage(chatId, "Please enter your username (3-50 characters):");
            }
        } else if (userInfo[3] == null) {
            if (input.length() < 3 || input.length() > 50) {
                sendMessage(chatId, "Please enter a valid username (3-50 characters):");
            } else {
                userInfo[3] = input; // Имя пользователя
                sendMessage(chatId, "Please enter your password (6-255 characters, letters, digits, and special characters allowed @#$%^&+=_-):");
            }
        } else if (userInfo[4] == null) {
            if (input.length() < 6 || input.length() > 255) {
                sendMessage(chatId, "Please enter a valid password (6-255 characters):");
            } else {
                userInfo[4] = input; // Пароль
                registerUser(chatId, userInfo); // Завершаем регистрацию
            }
        } else {
            sendMessage(chatId, "Registration process is complete.");
        }
    }


    private void registerUser(Long chatId, String[] userInfo) {
        String phoneNumber = userInfo[0];
        String firstName = userInfo[1];
        String lastName = userInfo[2];
        String username = userInfo[3];
        String password = userInfo[4];

        if (userRepository.findByUsername(username) != null) {
            sendMessage(chatId, "Username already exists! Please choose a different username.");
            userSession.removeUserInfo(chatId); // Удаляем данные из карты
            return;
        }

        userService.registerUser(firstName, lastName, username, password, chatId, phoneNumber);

        String info = "Registration successful! Welcome, " + firstName + "!";
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Login").callbackData("/login").build());

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(row1);
        markup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(info);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error occurred while sending message to chat ID: {}", chatId, e);
        }

        userSession.removeUserInfo(chatId); // Удаляем данные после завершения регистрации
    }


    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error occurred while sending message to chat ID: {}", chatId, e);
        }
    }

    private void sendMessage(Long chatId, SendMessage message) {
        try {
            execute(message); // Отправка сообщения с кнопками
        } catch (TelegramApiException e) {
            logger.error("Ошибка при отправке сообщения в чат ID: {}", chatId, e);
        }
    }

}
