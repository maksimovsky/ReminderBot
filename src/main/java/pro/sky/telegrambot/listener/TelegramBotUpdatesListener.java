package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final NotificationRepository repository;
    private final String defaultText =
            "Чтобы добавить напоминание, отправь сообщение вида: "
                    + "\"08.08.2023 15:00 Текст напоминания\"";

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            String messageText = update.message().text();
            Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)(.+)");
            Matcher matcher = pattern.matcher(messageText);
            if (matcher.matches()) {
                createNotification(matcher, update);
            } else if (messageText.equals("/start")) {
                sendGreetings(update);
            } else {
                sendWrongFormat(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendGreetings(Update update) {
        String messageText = "Привет, " + update.message().from().firstName() + "!\n"
                + "Добро пожаловать в бот-напоминалку!\n" + defaultText;
        sendMessage(update.message().chat().id(), messageText);
    }

    private void sendWrongFormat(Update update) {
        String messageText = "Неверный формат\n" + defaultText;
        sendMessage(update.message().chat().id(), messageText);
    }

    private void createNotification(Matcher matcher, Update update) {
        logger.info("Creating notification");
        String dateAndTime = matcher.group(1);
        String notificationText = matcher.group(3);
        LocalDateTime time = LocalDateTime.parse
                (dateAndTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        String messageText = "";

        switch (validateTime(time)) {
            case 1:
                NotificationTask notification = new NotificationTask();
                notification.setNotification(notificationText);
                notification.setChatId(update.message().chat().id());
                notification.setTime(time);
                repository.save(notification);
                messageText = "Напоминание создано!\n"
                        + time.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        + " в " + time.toLocalTime()
                        + " пришлю напоминание: \"" + notificationText + "\"";
                break;
            case 0:
                messageText = notificationText;
                break;
            case -1:
                messageText = "Пока не умею отправлять напоминания в прошлое";
                break;
        }
        sendMessage(update.message().chat().id(), messageText);
    }

    private int validateTime(LocalDateTime time) {
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        time = time.truncatedTo(ChronoUnit.MINUTES);
        if (time.isAfter(currentTime)) {
            return 1;
        } else if (currentTime.equals(time)) {
            return 0;
        }
        return -1;
    }

    private void sendMessage(Long chatId, String messageText) {
        logger.info("Sending message");
        SendMessage message = new SendMessage(chatId, messageText);
        telegramBot.execute(message);
    }

    @Scheduled(cron = "1 0/1 * * * *")
    private void check() {
        logger.info("Checking");
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Collection<NotificationTask> tasks = repository.findByTime(currentTime);
        tasks.forEach(task -> sendMessage(task.getChatId(), task.getNotification()));
    }
}