package com.notification.kafka.consumer;

import com.notification.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetupEventConsumer {

    private final NotificationService notificationService;

    /**
     * meetup.joined — 호스트에게 참가 알림
     */
    @KafkaListener(topics = "meetup.joined", groupId = "notification-service-group")
    public void handleMeetupJoined(Map<String, Object> payload) {
        try {
            Long meetupId      = Long.valueOf(payload.get("meetupId").toString());
            Long memberId      = Long.valueOf(payload.get("memberId").toString());
            Long hostMemberId  = Long.valueOf(payload.get("hostMemberId").toString());
            log.info("[Kafka] meetup.joined consumed: meetupId={}, memberId={}", meetupId, memberId);

            // 호스트에게 알림 (본인이 참가한 경우 제외)
            if (!memberId.equals(hostMemberId)) {
                notificationService.createNotification(
                        hostMemberId,
                        "MEETUP_JOINED",
                        "내 모임에 새로운 참가자가 생겼습니다.",
                        memberId, null, meetupId
                );
            }
        } catch (Exception e) {
            log.error("[Kafka] meetup.joined 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * meetup.canceled — 참가자 전원에게 취소 알림 일괄 발송
     */
    @KafkaListener(topics = "meetup.canceled", groupId = "notification-service-group")
    public void handleMeetupCanceled(Map<String, Object> payload) {
        try {
            Long meetupId     = Long.valueOf(payload.get("meetupId").toString());
            Long hostMemberId = Long.valueOf(payload.get("hostMemberId").toString());

            // participantMemberIds가 있으면 일괄 발송 (Meetup Service에서 포함 가능)
            Object participantsObj = payload.get("participantMemberIds");
            if (participantsObj instanceof List<?> participants) {
                log.info("[Kafka] meetup.canceled consumed: meetupId={}, participants={}",
                        meetupId, participants.size());
                for (Object p : participants) {
                    Long participantId = Long.valueOf(p.toString());
                    if (!participantId.equals(hostMemberId)) {
                        notificationService.createNotification(
                                participantId,
                                "MEETUP_CANCELED",
                                "참가 중인 모임이 취소되었습니다.",
                                hostMemberId, null, meetupId
                        );
                    }
                }
            } else {
                log.warn("[Kafka] meetup.canceled: participantMemberIds 없음 — meetupId={}", meetupId);
            }
        } catch (Exception e) {
            log.error("[Kafka] meetup.canceled 처리 실패: {}", e.getMessage(), e);
        }
    }
}
