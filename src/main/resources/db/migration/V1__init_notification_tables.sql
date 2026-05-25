CREATE TABLE IF NOT EXISTS notification (
    id              BIGINT        NOT NULL AUTO_INCREMENT       COMMENT '알림 PK',
    member_id       BIGINT        NOT NULL                      COMMENT '수신자 member_id',
    type            VARCHAR(30)   NOT NULL                      COMMENT '알림 유형',
    message         TEXT          NOT NULL                      COMMENT '알림 내용',
    from_member_id  BIGINT        NULL                          COMMENT '발신자 member_id',
    post_id         BIGINT        NULL                          COMMENT '관련 게시글 ID',
    meetup_id       BIGINT        NULL                          COMMENT '관련 모임 ID',
    is_read         TINYINT(1)    NOT NULL DEFAULT 0            COMMENT '읽음 여부',
    read_at         DATETIME(3)   NULL,
    created_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_noti_member      (member_id),
    KEY idx_noti_type        (type),
    KEY idx_noti_is_read     (is_read),
    KEY idx_noti_member_read (member_id, is_read),
    KEY idx_noti_post        (post_id),
    KEY idx_noti_meetup      (meetup_id),
    KEY idx_noti_created     (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림';

CREATE TABLE IF NOT EXISTS notification_delivery (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '발송 결과 PK',
    notification_id BIGINT        NOT NULL                 COMMENT '알림 FK',
    channel         VARCHAR(10)   NOT NULL                 COMMENT 'IN_APP|EMAIL|PUSH',
    status          VARCHAR(10)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|SENT|FAILED',
    sent_at         DATETIME(3)   NULL,
    error_message   TEXT          NULL,
    PRIMARY KEY (id),
    KEY idx_delivery_noti    (notification_id),
    KEY idx_delivery_status  (status),
    KEY idx_delivery_channel (channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 채널별 발송 결과';
