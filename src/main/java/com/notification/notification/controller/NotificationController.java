package com.notification.notification.controller;

import com.notification.notification.dto.NotificationResponse;
import com.notification.notification.dto.UnreadCountResponse;
import com.notification.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회", description = "미읽음 알림을 먼저 정렬하여 반환합니다.")
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestHeader("X-Member-Id") Long memberId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(memberId, pageable));
    }

    @Operation(summary = "미읽음 알림 수 조회", description = "미읽음 배지 카운트에 사용합니다.")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        return ResponseEntity.ok(notificationService.getUnreadCount(memberId));
    }

    @Operation(summary = "개별 알림 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(memberId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 읽음 처리", description = "배치 UPDATE로 전체 알림을 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "개별 알림 삭제")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long notificationId
    ) {
        notificationService.deleteNotification(memberId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 알림 삭제")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        notificationService.deleteAllNotifications(memberId);
        return ResponseEntity.noContent().build();
    }
}
