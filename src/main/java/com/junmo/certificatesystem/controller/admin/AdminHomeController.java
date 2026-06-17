package com.junmo.certificatesystem.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.junmo.certificatesystem.controller.HomeController;

/**
 * 관리자(ADMIN) 메인 화면 컨트롤러.
 *
 * FR-02 요구사항:
 * - 관리자 계정은 사용자와 다른 메뉴/화면을 본다.
 * - /admin/** URL은 ADMIN만 접근 가능.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')") // 이 컨트롤러의 모든 메서드는 ADMIN 전용
public class AdminHomeController {

    /**
     * 관리자 메인 화면.
     * 반환값 "admin/home" -> templates/admin/home.html
     */
    @GetMapping("/admin/home")
    public String adminHome(Authentication authentication, Model model) {
        HomeController.addUserAttributes(authentication, model);
        return "admin/home";
    }
}
