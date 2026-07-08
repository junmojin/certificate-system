package com.junmo.certificatesystem.service.audit;

import org.springframework.stereotype.Service;

import com.junmo.certificatesystem.common.enums.ActivityType;
import com.junmo.certificatesystem.entity.ActivityLog;
import com.junmo.certificatesystem.repository.ActivityLogRepository;
import com.junmo.certificatesystem.repository.UserRepository;

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
    private final UserRepository userRepository;

    public void record(String userId, String menuName, String requestUri, ActivityType activityType) {
        if (userId == null || userId.isBlank() || menuName == null || menuName.isBlank()) {
            return;
        }

        try {
            var actor = userRepository.findByUserId(userId.trim()).orElse(null);
            if (actor == null) {
                log.warn("행위 로그 저장 생략 - 사용자 없음: userId={}", userId);
                return;
            }

            ActivityLog activityLog = new ActivityLog();
            activityLog.setActorId(actor.getId());
            activityLog.setUserId(actor.getUserId());
            activityLog.setMenuName(menuName.trim());
            activityLog.setRequestUri(requestUri);
            activityLog.setActivityType(activityType != null ? activityType : ActivityType.MENU_ACCESS);
            activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.warn("행위 로그 저장 실패 - userId={}, menuName={}", userId, menuName, e);
        }
    }
}
