package com.junmo.certificatesystem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * FR-02: 증명서 신청 메뉴는 USER 전용.
 * (실제 신청 기능은 FR-03에서 구현 예정, 현재는 placeholder 화면)
 */
@Controller
@PreAuthorize("hasRole('USER')")
public class CertificateController {

    @GetMapping("/certificate")
    public String certificate() {
        return "certificate/apply";
    }
}
