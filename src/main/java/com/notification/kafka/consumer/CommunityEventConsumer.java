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
public class CommunityEventConsumer {

    private final NotificationService notificationService;

    /**
     * post.commented — 게시글 작성자 & 멘션 대상에게 댓글 알림
     */
    @KafkaListener(topics = "post.commented", groupId = "notification-service-group")
    public void handlePostCommented(Map<String, Object> payload) {
        try {
            Long postId          = Long.valueOf(payload.get("postId").toString());
            Long writerMemberId  = Long.valueOf(payload.get("writerMemberId").toString());
            Long commenterMemberId = Long.valueOf(payload.get("commenterMemberId").toString());
            Long mentionMemberId = payload.containsKey("mentionMemberId")
                    ? Long.valueOf(payload.get("mentionMemberId").toString()) : null;

            log.info("[Kafka] post.commented consumed: postId={}", postId);

            // 게시글 작성자에게 알림 (본인 댓글이면 제외)
            if (!writerMemberId.equals(commenterMemberId)) {
                notificationService.createNotification(
                        writerMemberId,
                        "POST_COMMENTED",
                        "내 게시글에 댓글이 달렸습니다.",
                        commenterMemberId, postId, null
                );
            }

            // 멘션 대상에게 알림
            if (mentionMemberId != null && !mentionMemberId.equals(commenterMemberId)) {
                notificationService.createNotification(
                        mentionMemberId,
                        "COMMENT_MENTIONED",
                        "댓글에서 나를 멘션했습니다.",
                        commenterMemberId, postId, null
                );
            }
        } catch (Exception e) {
            log.error("[Kafka] post.commented 처리 실패: {}", e.getMessage(), e);
        }
    }
}
