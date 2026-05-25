package com.notification.notification.service;

import com.notification.delivery.domain.NotificationDelivery;
import com.notification.delivery.repository.NotificationDeliveryRepository;
import com.notification.global.exception.BusinessException;
import com.notification.global.exception.ErrorCode;
import com.notification.notification.domain.Notification;
import com.notification.notification.dto.NotificationResponse;
import com.notification.notification.dto.UnreadCountResponse;
import com.notification.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationSender notificationSender;

    // ─── 알림 생성 (내부 전용) ────────────────────────────────────────────────

    @Transactional
    public Notification createNotification(Long memberId, String type, String message,
                                           Long fromMemberId, Long postId, Long meetupId) {
        Notification notification = Notification.builder()
                .memberId(memberId)
                .type(type)
                .message(message)
                .fromMemberId(fromMemberId)
                .postId(postId)
                .meetupId(meetupId)
                .build();
        notificationRepository.save(notification);

        // IN_APP 발송 기록
        saveDelivery(notification.getId(), "IN_APP");

        // 비동기 EMAIL 발송 (필요 시 활성화)
        // notificationSender.sendEmailAsync(notification);

        return notification;
    }

    // ─── 내 알림 목록 조회 ───────────────────────────────────────────────────

    public Page<NotificationResponse> getNotifications(Long memberId, Pageable pageable) {
        return notificationRepository.findAllByMemberId(memberId, pageable)
                .map(NotificationResponse::from);
    }

    // ─── 미읽음 배지 카운트 ──────────────────────────────────────────────────

    public UnreadCountResponse getUnreadCount(Long memberId) {
        return new UnreadCountResponse(
                notificationRepository.countByMemberIdAndIsRead(memberId, false)
        );
    }

    // ─── 개별 읽음 처리 ──────────────────────────────────────────────────────

    @Transactional
    public void markAsRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.validateOwner(memberId);
        notification.markAsRead();
    }

    // ─── 전체 읽음 처리 (배치 UPDATE) ────────────────────────────────────────

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsRead(memberId);
    }

    // ─── 개별 삭제 ───────────────────────────────────────────────────────────

    @Transactional
    public void deleteNotification(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.validateOwner(memberId);
        notificationRepository.delete(notification);
    }

    // ─── 전체 삭제 ───────────────────────────────────────────────────────────

    @Transactional
    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteAllByMemberId(memberId);
    }

    // ─── 공통 ────────────────────────────────────────────────────────────────

    private void saveDelivery(Long notificationId, String channel) {
        deliveryRepository.save(
                NotificationDelivery.builder()
                        .notificationId(notificationId)
                        .channel(channel)
                        .build()
        );
    }
}
