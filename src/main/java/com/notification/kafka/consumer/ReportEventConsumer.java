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
public class ReportEventConsumer {

    private final NotificationService notificationService;

    // 관리자 member_id — 실제 운영 시 Config 또는 환경변수로 관리 권장
    private static final Long ADMIN_MEMBER_ID = 1L;

    /**
     * report.created — 관리자에게 신고 접수 알림
     */
    @KafkaListener(topics = "report.created", groupId = "notification-service-group")
    public void handleReportCreated(Map<String, Object> payload) {
        try {
            Long reportId   = Long.valueOf(payload.get("reportId").toString());
            String targetType = payload.get("targetType").toString();
            log.info("[Kafka] report.created consumed: reportId={}", reportId);

            notificationService.createNotification(
                    ADMIN_MEMBER_ID,
                    "REPORT_RECEIVED",
                    "[" + targetType + "] 신고가 접수되었습니다. (신고 ID: " + reportId + ")",
                    null, null, null
            );
        } catch (Exception e) {
            log.error("[Kafka] report.created 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * report.resolved — 신고자 & 피신고자에게 처리 결과 알림
     */
    @KafkaListener(topics = "report.resolved", groupId = "notification-service-group")
    public void handleReportResolved(Map<String, Object> payload) {
        try {
            Long reportId              = Long.valueOf(payload.get("reportId").toString());
            String status              = payload.get("status").toString();
            Long reporterMemberId      = Long.valueOf(payload.get("reporterMemberId").toString());
            Long reportedWriterMemberId = Long.valueOf(payload.get("reportedWriterMemberId").toString());
            log.info("[Kafka] report.resolved consumed: reportId={}, status={}", reportId, status);

            String resultMsg = "RESOLVED".equals(status) ? "처리 완료" : "반려";

            // 신고자에게 알림
            if (reporterMemberId > 0) {
                notificationService.createNotification(
                        reporterMemberId,
                        "REPORT_RESULT",
                        "접수하신 신고가 " + resultMsg + "되었습니다.",
                        null, null, null
                );
            }

            // 피신고자에게 알림 (RESOLVED인 경우만)
            if ("RESOLVED".equals(status) && reportedWriterMemberId > 0) {
                notificationService.createNotification(
                        reportedWriterMemberId,
                        "REPORT_ACTION_TAKEN",
                        "작성하신 콘텐츠에 대한 신고가 처리되었습니다.",
                        null, null, null
                );
            }
        } catch (Exception e) {
            log.error("[Kafka] report.resolved 처리 실패: {}", e.getMessage(), e);
        }
    }
}
