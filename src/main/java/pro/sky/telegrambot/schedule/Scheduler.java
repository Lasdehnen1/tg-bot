package pro.sky.telegrambot.schedule;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;
import pro.sky.telegrambot.service.NotificationService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class Scheduler {

    private final NotificationService notificationService;
    private final TelegramBot telegramBot;

    public Scheduler(NotificationService notificationService, TelegramBot telegramBot) {
        this.notificationService = notificationService;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void scheduleNotify() {
        LocalDateTime localTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> taskList = notificationService.findNotification(localTime);

        taskList.forEach(n -> {
            telegramBot.execute(new SendMessage(n.getChat_id(), "Напоминание: " + n.getMessage()));
        });

    }
}
