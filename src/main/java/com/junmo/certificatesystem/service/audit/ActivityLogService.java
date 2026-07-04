package com.junmo.certificatesystem.service.audit;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.junmo.certificatesystem.common.enums.RoleType;
import com.junmo.certificatesystem.dto.audit.ActivityLogResponse;
import com.junmo.certificatesystem.entity.ActivityLog;
import com.junmo.certificatesystem.entity.User;
import com.junmo.certificatesystem.repository.ActivityLogRepository;
import com.junmo.certificatesystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityLogService {

    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public List<ActivityLogResponse> search(String userId, LocalDate logDate) {
        String filterUserId = isBlank(userId) ? null : userId.trim();
        LocalDate filterDate = logDate;

        return activityLogRepository.search(filterUserId, filterDate).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<String> getUserIds() {
        return userRepository.findByRoleOrderByUserIdAsc(RoleType.USER).stream()
                .map(User::getUserId)
                .toList();
    }

    private ActivityLogResponse toResponse(ActivityLog activityLog) {
        if (activityLog.getCreatedAt() == null) {
            return ActivityLogResponse.builder()
                    .id(activityLog.getId())
                    .occurredAt(null)
                    .userId(activityLog.getUserId())
                    .menuName(activityLog.getMenuName())
                    .formattedLog(activityLog.getUserId() + " / " + activityLog.getMenuName())
                    .build();
        }

        String formattedLog = LOG_TIME_FORMAT.format(activityLog.getCreatedAt())
                + " / " + activityLog.getUserId()
                + " / " + activityLog.getMenuName();

        return ActivityLogResponse.builder()
                .id(activityLog.getId())
                .occurredAt(activityLog.getCreatedAt())
                .userId(activityLog.getUserId())
                .menuName(activityLog.getMenuName())
                .formattedLog(formattedLog)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
