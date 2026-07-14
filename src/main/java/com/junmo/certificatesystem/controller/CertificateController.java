package com.junmo.certificatesystem.controller;

import java.io.IOException;
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

import com.junmo.certificatesystem.config.properties.SmartCertProperties;
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
 * 승인된 증명서는 HTML5 PDF 뷰어({@code html5view/np_reader.jsp})로 연다.
 */
@Slf4j
@Controller
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CertificateController {

    private final CarrerCertificateService carrerCertificateService;
    private final SmartCertProperties smartCertProperties;

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

    /** 경력증명서 — 승인 시 HTML5 뷰어, 그 외 HTML 안내 */
    @GetMapping("/certificate/{id}/view")
    @Transactional(readOnly = true)
    public String viewCertificate(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        CertificateViewData viewData = carrerCertificateService.getOwnCertificateView(
                id, userDetails.getUsername());

        if (carrerCertificateService.isViewableInPdfViewer(viewData.getStatus())) {
            try {
                carrerCertificateService.ensureOwnCertificatePdfFile(id, userDetails.getUsername());
                return "redirect:/html5view/np_reader.jsp"
                        + "?docno=" + id
                        + "&bcd=" + smartCertProperties.getViewerBarcode()
                        + "&ve=" + smartCertProperties.getViewerVoiceye()
                        + "&wm=ok";
            } catch (IOException e) {
                log.error("경력증명서 PDF 파일 생성 실패 - certificateId={}", id, e);
                HomeController.addUserAttributes(authentication, model);
                addViewAttributes(model, viewData);
                model.addAttribute("pdfError",
                        "PDF 파일을 생성하지 못했습니다. app.smart-cert.pdf-dir 경로("
                                + smartCertProperties.getPdfDir() + ")를 확인해 주세요.");
                return "certificate/view";
            }
        }

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
