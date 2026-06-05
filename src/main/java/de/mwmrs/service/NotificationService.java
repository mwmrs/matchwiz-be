package de.mwmrs.service;

import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.Notification;
import de.mwmrs.entity.NotificationType;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class NotificationService {

    @Transactional
    public Notification create(AppUser user, NotificationType type, String title, String message) {
        Notification n = new Notification();
        n.user = user;
        n.type = type;
        n.title = title;
        n.message = message;
        n.persist();
        return n;
    }

    public List<Notification> listForUser(Long userId) {
        return Notification.list("user.id = ?1 order by createdAt desc", userId);
    }

    @Transactional
    public Notification markRead(Long id, Long userId) {
        Notification n = Notification.findById(id);
        if (n == null || !n.user.id.equals(userId)) {
            throw BusinessException.notFound("Notification not found");
        }
        n.read = true;
        return n;
    }
}
