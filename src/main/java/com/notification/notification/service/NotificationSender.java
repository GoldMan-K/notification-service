package com.notification.notification.service;

import com.notification.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

    private final JavaMailSender mailSender;

    /**
     * 이메일 비동기 발송
     * NotificationService에서 필요 시 호출
     */
    @Async
    public void sendEmailAsync(Notification notification, String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[충북모먼트] " + notification.getType());
            message.setText(notification.getMessage());
            mailSender.send(message);
            log.info("[Email] 발송 성공: notificationId={}, to={}", notification.getId(), toEmail);
        } catch (Exception e) {
            log.error("[Email] 발송 실패: notificationId={}, error={}", notification.getId(), e.getMessage());
        }
    }
}
