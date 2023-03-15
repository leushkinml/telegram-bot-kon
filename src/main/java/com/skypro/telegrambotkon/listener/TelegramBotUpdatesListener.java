package com.skypro.telegrambotkon.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.skypro.telegrambotkon.service.NotificationTaskService;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private static final Pattern NOTIFICATION_TASK_PATTERN = Pattern.compile(
       "([\\d\\\\.:\\s]{16})(\\s)([А-яA-z\\s\\d,.!?:]+)" // всего 4 группы: 0-ая общая, и три внутри.
    );

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }


    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                LOG.info("Processing update: {}", update);
                String text = update.message().text();
//                Long userId = update.message().chat().id();
                Long userId = update.message().from().id();
                if("/start".equals(text)) { //последовательность такая, чтобы иквлс обработал text если будет null и не выбросилось исключение.
                    SendMessage sendMessage = new SendMessage(userId,
                            "Для планирования задачи отправьте её в формате:\n*01.01.2022 20:00 Сделать домашнюю работу*");
                    sendMessage.parseMode(ParseMode.Markdown);
                    telegramBot.execute(sendMessage);
                } else if (text != null) {
                    Matcher matcher = NOTIFICATION_TASK_PATTERN.matcher(text);
                    if (matcher.find()) {
                        LocalDateTime localDateTime = parse(matcher.group(1));
                        if (!Objects.isNull(localDateTime)) {
                            String message = matcher.group(3);
                            notificationTaskService.addNotificationTask(localDateTime, message, userId);
                            telegramBot.execute(new SendMessage(userId, "Ваша задача запланирована!"));
                        } else {
                            telegramBot.execute(new SendMessage(userId, "Некорректный формат даты и/или времени!"));
                        }
                    } else {
                        telegramBot.execute(new SendMessage(userId, "Некорректный формат задачи для планирования! Корректный формат: 01.01.2022 20:00 Сделать домашнюю работу"));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}