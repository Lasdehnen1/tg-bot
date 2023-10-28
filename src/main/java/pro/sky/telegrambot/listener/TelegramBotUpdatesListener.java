package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final static String TEXT = "Hello ma friend. This bot is to plan. Use that template: 01.01.2022 20:00 Сделать домашнюю работу";
    private final static String TEXT2 = "use only that format 01.01.2022 20:00 Сделать домашнюю работу";
    private final static Pattern PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");

    private final TelegramBot telegramBot;
    private final NotificationRepository notificationRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationRepository notificationRepository) {
        this.telegramBot = telegramBot;
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {

                logger.info("Processing update: {}", update);
                // Process your updates here
                String message = update.message().text();

                if (message.equals("/start")) {
                    SendMessage newMessage = new SendMessage(update.message().chat().id(), TEXT);
                    SendResponse response = telegramBot.execute(newMessage);
                } else {
                    Matcher matcher = PATTERN.matcher(message);
                    if (matcher.matches()) {
                        // обрабатываем ситуацию, когда строка соответствует паттерну
//                        String dateString = message.substring(0, 16);
//                        String messageString = message.substring(17);
                        String dateString = matcher.group(1);
                        String messageString = matcher.group(3);
                        LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                        Long chatId = update.message().chat().id();
                        NotificationTask notificationTask = new NotificationTask(chatId, messageString, dateTime);
                        logger.info("сохранение оповещения");
                        notificationRepository.save(notificationTask);
                    } else {
                        SendMessage errorMessage = new SendMessage(update.message().chat().id(), TEXT2);
                        logger.info("неверный формат ввода сообщения");
                        telegramBot.execute(errorMessage);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
