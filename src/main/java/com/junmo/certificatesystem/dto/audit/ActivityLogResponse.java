package com.junmo.certificatesystem.dto.audit;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityLogResponse {

    private final Long id;
    private final LocalDateTime occurredAt;
    private final String userId;
    private final String menuName;
    private final String formattedLog;
}
