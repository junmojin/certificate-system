package com.junmo.certificatesystem.entity;

import com.junmo.certificatesystem.common.entity.BaseTimeEntity;
import com.junmo.certificatesystem.common.enums.ActivityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_logs_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "request_uri", length = 255)
    private String requestUri;
}
