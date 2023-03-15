package com.skypro.telegrambotkon.timer;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.skypro.telegrambotkon.service.NotificationTaskService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NotificationTaskTimer {

    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;

    public NotificationTaskTimer(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }

//    @Scheduled(fixedDelay = 60 * 1_000)
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void checkNotifications() {
        notificationTaskService.findNotificationsForSend().forEach(notificationTask -> {
            telegramBot.execute(
                    new SendMessage(notificationTask.getUserId(), "Вы просили напомнить об этом: " + notificationTask.getMessage())
            );
            notificationTaskService.deleteTask(notificationTask);
        });
    }
}
