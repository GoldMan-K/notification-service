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
     * post.commented — 게시글 작성자 댓글 알림 + 부모 댓글 작성자 대댓글 알림
     */
    @KafkaListener(topics = "post.commented", groupId = "notification-service-group")
    public void handlePostCommented(Map<String, Object> payload) {
        try {
            log.info("[Kafka] post.commented payload={}", payload);

            Long postId = requiredLong(payload, "postId");
            Long actorMemberId = requiredLong(payload, "memberId", "commenterMemberId", "fromMemberId");
            if (postId == null || actorMemberId == null) {
                log.warn("[Kafka] post.commented 필수 필드(postId/memberId) 누락으로 처리 중단: payload={}", payload);
                return;
            }

            Long postWriterMemberId = optionalLong(payload, "postWriterMemberId", "writerMemberId");
            Long parentCommentWriterMemberId = optionalLong(payload,
                    "parentCommentWriterMemberId", "parentCommentMemberId", "parentMemberId", "replyTargetMemberId");

            boolean created = false;

            log.info("[Kafka] post.commented consumed: postId={}", postId);

            // 댓글 알림: 다른 사용자가 내 게시글에 댓글 작성
            if (postWriterMemberId != null && !postWriterMemberId.equals(actorMemberId)) {
                notificationService.createNotification(
                        postWriterMemberId,
                        "COMMENT",
                        "내 게시글에 댓글이 달렸습니다.",
                        actorMemberId, postId, null
                );
                created = true;
            }

            // 대댓글 알림: 다른 사용자가 내 댓글에 대댓글 작성
            if (parentCommentWriterMemberId != null && !parentCommentWriterMemberId.equals(actorMemberId)) {
                notificationService.createNotification(
                        parentCommentWriterMemberId,
                        "REPLY",
                        "내 댓글에 대댓글이 달렸습니다.",
                        actorMemberId, postId, null
                );
                created = true;
            }

            if (!created) {
                log.warn("[Kafka] post.commented 알림 대상이 없어 생성 생략: payload={}", payload);
            }
        } catch (Exception e) {
            log.error("[Kafka] post.commented 처리 실패: {}", e.getMessage(), e);
        }
    }

    private Long requiredLong(Map<String, Object> payload, String... keys) {
        Long value = optionalLong(payload, keys);
        if (value == null) {
            log.warn("[Kafka] 필수 필드 누락: keys={}", String.join(",", keys));
        }
        return value;
    }

    private Long optionalLong(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                return Long.valueOf(value.toString());
            }
        }
        return null;
    }
}
