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
public class InquiryEventConsumer {

    private final NotificationService notificationService;

    // 관리자 member_id — 실제 운영 시 Config 또는 환경변수로 관리 권장
    private static final Long ADMIN_MEMBER_ID = 1L;

    /**
     * inquiry.created — 작성자에게 접수 확인 + 관리자에게 새 문의 알림
     */
    @KafkaListener(topics = "inquiry.created", groupId = "notification-service-group")
    public void handleInquiryCreated(Map<String, Object> payload) {
        try {
            log.info("[Kafka] inquiry.created payload={}", payload);

            Object inquiryIdVal = payload.get("inquiryId");
            Object memberIdVal  = payload.get("memberId");
            if (inquiryIdVal == null || memberIdVal == null) {
                log.warn("[Kafka] inquiry.created 필수 필드(inquiryId/memberId) 누락으로 처리 중단: payload={}", payload);
                return;
            }

            Long inquiryId = Long.valueOf(inquiryIdVal.toString());
            Long memberId  = Long.valueOf(memberIdVal.toString());
            String title   = payload.getOrDefault("title", "").toString();

            log.info("[Kafka] inquiry.created consumed: inquiryId={}, memberId={}", inquiryId, memberId);

            // 1) 작성자 → 문의 접수 확인 알림 (비회원 제외)
            if (memberId > 0) {
                notificationService.createNotification(
                        memberId,
                        "INQUIRY_RECEIVED",
                        "문의가 정상 접수되었습니다.",
                        null, null, null
                );
            }

            // 2) 관리자 → 새 문의 접수 알림
            notificationService.createNotification(
                    ADMIN_MEMBER_ID,
                    "ADMIN_NEW_INQUIRY",
                    "새 문의가 접수되었습니다: " + title,
                    null, null, null
            );

        } catch (Exception e) {
            log.error("[Kafka] inquiry.created 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * inquiry.answered — 작성자에게 답변 완료 알림
     */
    @KafkaListener(topics = "inquiry.answered", groupId = "notification-service-group")
    public void handleInquiryAnswered(Map<String, Object> payload) {
        try {
            log.info("[Kafka] inquiry.answered payload={}", payload);

            Object inquiryIdVal = payload.get("inquiryId");
            Object memberIdVal  = payload.get("memberId");
            if (inquiryIdVal == null || memberIdVal == null) {
                log.warn("[Kafka] inquiry.answered 필수 필드(inquiryId/memberId) 누락으로 처리 중단: payload={}", payload);
                return;
            }

            Long inquiryId = Long.valueOf(inquiryIdVal.toString());
            Long memberId  = Long.valueOf(memberIdVal.toString());
            String status  = payload.getOrDefault("status", "ANSWERED").toString();

            log.info("[Kafka] inquiry.answered consumed: inquiryId={}, status={}", inquiryId, status);

            String message = "CLOSED".equals(status)
                    ? "문의가 종료되었습니다."
                    : "문의에 답변이 등록되었습니다.";

            // 작성자 → 답변 완료 알림 (비회원 제외)
            if (memberId > 0) {
                notificationService.createNotification(
                        memberId,
                        "INQUIRY_ANSWERED",
                        message,
                        null, null, null
                );
            }

        } catch (Exception e) {
            log.error("[Kafka] inquiry.answered 처리 실패: {}", e.getMessage(), e);
        }
    }
}

