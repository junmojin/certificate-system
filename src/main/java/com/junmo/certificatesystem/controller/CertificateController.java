package com.junmo.certificatesystem.controller;

import java.util.Collections;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.junmo.certificatesystem.controller.HomeController;
import com.junmo.certificatesystem.dto.certificate.CertificateApplyRequest;
import com.junmo.certificatesystem.dto.certificate.CertificateViewData;
import com.junmo.certificatesystem.entity.CarrerCertificate;
import com.junmo.certificatesystem.security.CustomUserDetails;
import com.junmo.certificatesystem.service.certificate.CarrerCertificateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FR-03 증명서 신청.
 * 화면 뷰어({@code certificate/view.html})에서 경력증명서를 확인·인쇄한다.
 * PDFBox 서식 구현은 {@link com.junmo.certificatesystem.service.certificate.CareerCertificatePdfService} 참고.
 */
@Slf4j
@Controller
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CertificateController {

    private final CarrerCertificateService carrerCertificateService;

    @GetMapping("/certificate")
    @Transactional(readOnly = true)
    public String applyForm(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        addApplyFormAttributes(model, userDetails);
        model.addAttribute("certificateApplyRequest", new CertificateApplyRequest());
        return "certificate/apply";
    }

    @PostMapping("/certificate")
    @Transactional
    public String apply(
            @Valid @ModelAttribute CertificateApplyRequest certificateApplyRequest,
            BindingResult bindingResult,
            Authentication authentication,
            Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (bindingResult.hasErrors()) {
            addApplyFormAttributes(model, userDetails);
            return "certificate/apply";
        }

        CarrerCertificate saved = carrerCertificateService.apply(
                userDetails.getUser(), certificateApplyRequest);

        return "redirect:/certificate/" + saved.getId() + "/view";
    }

    /** 경력증명서 뷰어 (인쇄 가능) */
    @GetMapping("/certificate/{id}/view")
    @Transactional(readOnly = true)
    public String viewCertificate(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        CertificateViewData viewData = carrerCertificateService.getOwnCertificateView(
                id, userDetails.getUsername());

        HomeController.addUserAttributes(authentication, model);
        addViewAttributes(model, viewData);
        return "certificate/view";
    }

    private void addApplyFormAttributes(Model model, CustomUserDetails userDetails) {
        model.addAttribute("userId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getUser().getName());
        model.addAttribute("hireDate", userDetails.getUser().getHireDate());
        try {
            model.addAttribute("myApplications",
                    carrerCertificateService.getMyApplicationSummaries(userDetails.getUsername()));
        } catch (Exception e) {
            log.error("증명서 신청 내역 조회 실패 - career_certificates 테이블 확인 필요", e);
            model.addAttribute("myApplications", Collections.emptyList());
            model.addAttribute("dbError",
                    "신청 내역을 불러오지 못했습니다. DB 테이블(career_certificates)을 확인해 주세요.");
        }
    }

    private void addViewAttributes(Model model, CertificateViewData viewData) {
        model.addAttribute("cert", viewData);
        model.addAttribute("footerStatement", CertificateViewData.FOOTER_STATEMENT);
        model.addAttribute("formattedIssueDate",
                carrerCertificateService.formatDocumentDate(viewData.getIssueDate()));
        model.addAttribute("formattedHireDate",
                carrerCertificateService.formatDocumentDate(viewData.getHireDate()));
    }
}
