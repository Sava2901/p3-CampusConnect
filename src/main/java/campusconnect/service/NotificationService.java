package campusconnect.service;

import campusconnect.db.DataStore;
import campusconnect.db.JsonDatabase;
import campusconnect.interfaces.Notifier;
import campusconnect.model.Notification;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService implements Notifier {
    private final JsonDatabase db;

    public NotificationService(JsonDatabase db) {
        this.db = db;
    }

    @Override
    public void notify(Notification notification) {
        DataStore s = db.load();
        long id = db.nextNotificationId(s);
        notification.setId(id);
        if (notification.getCreatedAt() == null) notification.setCreatedAt(Instant.now());
        s.getNotifications().add(notification);
        db.save(s);
    }

    public List<Notification> listByUser(long userId) {
        DataStore s = db.load();
        return s.getNotifications().stream().filter(n -> n.getUserId() == userId).collect(Collectors.toList());
    }

    public void markAllRead(long userId) {
        DataStore s = db.load();
        s.getNotifications().stream().filter(n -> n.getUserId() == userId).forEach(n -> n.setRead(true));
        db.save(s);
    }
}
