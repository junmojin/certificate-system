package com.junmo.certificatesystem.controller.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.junmo.certificatesystem.controller.HomeController;
import com.junmo.certificatesystem.service.audit.ActivityLogService;

import lombok.RequiredArgsConstructor;

/**
 * FR-07: 사용자 행위 추적 — ADMIN 전용 조회.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping("/admin/activity-logs")
    public String activityLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
            Authentication authentication,
            Model model) {

        HomeController.addUserAttributes(authentication, model);
        model.addAttribute("logs", activityLogService.search(userId, logDate));
        model.addAttribute("userIds", activityLogService.getUserIds());
        model.addAttribute("selectedUserId", userId == null ? "" : userId);
        model.addAttribute("selectedLogDate", logDate == null ? "" : logDate.toString());
        model.addAttribute("hasDateFilter", logDate != null);
        return "admin/activity-logs";
    }
}
