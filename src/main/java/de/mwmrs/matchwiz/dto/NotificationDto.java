package de.mwmrs.dto;

import de.mwmrs.entity.Notification;
import de.mwmrs.entity.NotificationType;
import java.time.OffsetDateTime;

public record NotificationDto(
        Long id,
        Long userId,
        NotificationType type,
        String title,
        String message,
        boolean read,
        OffsetDateTime createdAt) {

    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.id, n.user.id, n.type, n.title, n.message, n.read, n.createdAt);
    }
}
