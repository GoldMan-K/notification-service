package com.notification.delivery.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_delivery")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;
    private String channel;        // IN_APP | EMAIL | PUSH
    private String status;         // PENDING | SENT | FAILED
    private LocalDateTime sentAt;
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Builder
    public NotificationDelivery(Long notificationId, String channel) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = "PENDING";
    }

    public void markSent() {
        this.status = "SENT";
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
}
