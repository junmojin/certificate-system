package com.junmo.certificatesystem.service.audit;

import org.springframework.stereotype.Service;

import com.junmo.certificatesystem.entity.ActivityLog;
import com.junmo.certificatesystem.repository.ActivityLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FR-07 행위 로그 저장. 실패해도 로그인·업무 흐름에는 영향을 주지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogRecorder {

    private final ActivityLogRepository activityLogRepository;

    public void record(String userId, String menuName) {
        if (userId == null || userId.isBlank() || menuName == null || menuName.isBlank()) {
            return;
        }

        try {
            ActivityLog activityLog = new ActivityLog();
            activityLog.setUserId(userId.trim());
            activityLog.setMenuName(menuName.trim());
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.warn("행위 로그 저장 실패 - userId={}, menuName={}", userId, menuName, e);
        }
    }
}
