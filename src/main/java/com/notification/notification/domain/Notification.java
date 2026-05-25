package com.notification.notification.domain;

import com.notification.global.exception.BusinessException;
import com.notification.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;         // 수신자
    private String type;           // 알림 유형
    @Column(columnDefinition = "TEXT")
    private String message;
    private Long fromMemberId;     // 발신자 (없으면 null)
    private Long postId;           // 관련 게시글 (없으면 null)
    private Long meetupId;         // 관련 모임 (없으면 null)
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    @Builder
    public Notification(Long memberId, String type, String message,
                        Long fromMemberId, Long postId, Long meetupId) {
        this.memberId = memberId;
        this.type = type;
        this.message = message;
        this.fromMemberId = fromMemberId;
        this.postId = postId;
        this.meetupId = meetupId;
        this.isRead = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void validateOwner(Long memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
