package com.junmo.certificatesystem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.junmo.certificatesystem.security.CustomUserDetails;

/**
 * 일반 사용자(USER) 화면 컨트롤러.
 *
 * FR-02에서 담당:
 * - 사용자 메인(/home)
 * - 접근 거부 안내(/access-denied)
 * - 루트(/) 접속 시 계정 유형별 리다이렉트
 */
@Controller
public class HomeController {

    /**
     * http://localhost:8080/ 접속 시
     * ADMIN -> /admin/home
     * USER  -> /home
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/home" : "redirect:/home";
    }

    /**
     * 사용자 메인 화면.
     * @PreAuthorize: USER 권한이 아니면 접근 불가 (ADMIN이 직접 URL 입력 시 차단)
     * 반환값 "home" -> templates/home.html 렌더링
     */
    @GetMapping("/home")
    @PreAuthorize("hasRole('USER')")
    public String userHome(Authentication authentication, Model model) {
        addUserAttributes(authentication, model);
        return "home";
    }

    /** 권한 없는 메뉴 접근 시 보여주는 안내 화면 */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    /**
     * 화면에 이름/ID/권한을 넘겨주는 공통 메서드.
     * AdminHomeController에서도 재사용한다.
     */
    public static void addUserAttributes(Authentication authentication, Model model) {
        // principal: 현재 로그인 사용자 객체 (CustomUserDetails)
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getUser().getName());
        model.addAttribute("role", userDetails.getUser().getRole().name());
    }
}
