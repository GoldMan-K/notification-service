package com.notification.kafka.consumer;

import com.notification.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberEventConsumer {

    private final NotificationService notificationService;

    /**
     * member.created — 환영 알림 발송
     */
    @KafkaListener(topics = "member.created", groupId = "notification-service-group")
    public void handleMemberCreated(Map<String, Object> payload) {
        try {
            Long memberId = Long.valueOf(payload.get("memberId").toString());
            log.info("[Kafka] member.created consumed: memberId={}", memberId);

            notificationService.createNotification(
                    memberId,
                    "WELCOME",
                    "충북모먼트에 오신 것을 환영합니다! 지역 커뮤니티를 즐겨보세요.",
                    null, null, null
            );
        } catch (Exception e) {
            log.error("[Kafka] member.created 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * member.suspended — 계정 정지 알림 발송
     */
    @KafkaListener(topics = "member.suspended", groupId = "notification-service-group")
    public void handleMemberSuspended(Map<String, Object> payload) {
        try {
            Long memberId = Long.valueOf(payload.get("memberId").toString());
            String reason = payload.getOrDefault("reason", "").toString();
            log.info("[Kafka] member.suspended consumed: memberId={}", memberId);

            notificationService.createNotification(
                    memberId,
                    "ACCOUNT_SUSPENDED",
                    "계정이 정지되었습니다." + (reason.isBlank() ? "" : " 사유: " + reason),
                    null, null, null
            );
        } catch (Exception e) {
            log.error("[Kafka] member.suspended 처리 실패: {}", e.getMessage(), e);
        }
    }
}
