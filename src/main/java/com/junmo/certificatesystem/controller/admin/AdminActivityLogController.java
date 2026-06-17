package com.junmo.certificatesystem.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * FR-02: 사용자 행위 추적 메뉴는 ADMIN 전용.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityLogController {

    @GetMapping("/admin/activity-logs")
    public String activityLogs() {
        return "admin/activity-logs";
    }
}
