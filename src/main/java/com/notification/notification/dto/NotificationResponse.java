package com.notification.notification.dto;

import com.notification.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String message,
        Long fromMemberId,
        Long postId,
        Long meetupId,
        boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.getFromMemberId(),
                n.getPostId(),
                n.getMeetupId(),
                n.isRead(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
