package com.junmo.certificatesystem.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.junmo.certificatesystem.controller.HomeController;
import com.junmo.certificatesystem.service.certificate.CarrerCertificateService;

import lombok.RequiredArgsConstructor;

/**
 * FR-04: 관리자 증명서 신청 조회 및 발급 승인/거부.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCertificateController {

    private final CarrerCertificateService carrerCertificateService;

    @GetMapping("/admin/certificates")
    @Transactional(readOnly = true)
    public String certificateList(
            @RequestParam(required = false) String userId,
            Authentication authentication,
            Model model) {

        HomeController.addUserAttributes(authentication, model);
        model.addAttribute("applications", carrerCertificateService.getAdminApplicationSummaries(userId));
        model.addAttribute("applicantUserIds", carrerCertificateService.getApplicantUserIds());
        model.addAttribute("selectedUserId", userId == null ? "" : userId);
        return "admin/certificate-list";
    }

    @GetMapping("/admin/certificates/{id}")
    @Transactional(readOnly = true)
    public String certificateDetail(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        HomeController.addUserAttributes(authentication, model);

        try {
            model.addAttribute("certApplication", carrerCertificateService.getAdminApplicationDetail(id));
            return "admin/certificate-detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/certificates";
        }
    }

    @PostMapping("/admin/certificates/{id}/approve")
    @Transactional
    public String approveCertificate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "detail") String returnTo,
            @RequestParam(required = false) String userId,
            RedirectAttributes redirectAttributes) {

        try {
            carrerCertificateService.approveApplication(id);
            redirectAttributes.addFlashAttribute("successMessage", "발급 승인 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return buildReviewRedirect(returnTo, id, userId);
    }

    @PostMapping("/admin/certificates/{id}/reject")
    @Transactional
    public String rejectCertificate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "detail") String returnTo,
            @RequestParam(required = false) String userId,
            RedirectAttributes redirectAttributes) {

        try {
            carrerCertificateService.rejectApplication(id);
            redirectAttributes.addFlashAttribute("successMessage", "발급 거부 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return buildReviewRedirect(returnTo, id, userId);
    }

    private String buildReviewRedirect(String returnTo, Long id, String userId) {
        if ("list".equals(returnTo)) {
            if (userId != null && !userId.isBlank()) {
                return "redirect:/admin/certificates?userId=" + userId;
            }
            return "redirect:/admin/certificates";
        }
        return "redirect:/admin/certificates/" + id;
    }
}
