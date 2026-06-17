package com.junmo.certificatesystem.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * FR-02: 증명서 신청 조회 메뉴는 ADMIN 전용.
 * (승인/거부 기능은 FR-04에서 구현 예정)
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminCertificateController {

    @GetMapping("/admin/certificates")
    public String certificateList() {
        return "admin/certificate-list";
    }
}
