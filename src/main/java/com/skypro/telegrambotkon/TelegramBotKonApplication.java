package com.skypro.telegrambotkon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramBotKonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotKonApplication.class, args);
    }

}
