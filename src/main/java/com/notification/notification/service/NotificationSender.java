package com.notification.notification.service;

import com.notification.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSender {

    /**
     * 이메일 발송 비활성화 (메일 서버 미사용)
     */
    public void sendEmailAsync(Notification notification, String toEmail) {
        log.info("[Email] 메일 발송 비활성화 상태: notificationId={}, to={}", notification.getId(), toEmail);
    }
}
