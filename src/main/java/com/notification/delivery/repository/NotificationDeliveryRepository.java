package com.notification.delivery.repository;

import com.notification.delivery.domain.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    // FAILED 재발송 대상 조회
    List<NotificationDelivery> findAllByStatus(String status);
}
