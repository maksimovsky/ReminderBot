package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Long chatId;
    private String notification;
    private LocalDateTime time;

    public NotificationTask() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationTask that = (NotificationTask) o;

        if (id != that.id) return false;
        if (!Objects.equals(chatId, that.chatId)) return false;
        if (!Objects.equals(notification, that.notification)) return false;
        return Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (chatId != null ? chatId.hashCode() : 0);
        result = 31 * result + (notification != null ? notification.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", notification='" + notification + '\'' +
                ", time=" + time +
                '}';
    }
}
