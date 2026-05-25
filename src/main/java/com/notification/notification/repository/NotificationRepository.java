package com.notification.notification.repository;

import com.notification.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 미읽음 먼저, 최신순 정렬
    @Query("""
            SELECT n FROM Notification n
            WHERE n.memberId = :memberId
            ORDER BY n.isRead ASC, n.createdAt DESC
            """)
    Page<Notification> findAllByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // 미읽음 배지 카운트
    int countByMemberIdAndIsRead(Long memberId, boolean isRead);

    // 전체 읽음 처리 (배치 UPDATE)
    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP
            WHERE n.memberId = :memberId AND n.isRead = false
            """)
    int markAllAsRead(@Param("memberId") Long memberId);

    // 전체 삭제
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
