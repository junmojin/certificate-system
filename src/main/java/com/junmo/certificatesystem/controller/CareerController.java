package com.junmo.certificatesystem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.junmo.certificatesystem.security.CustomUserDetails;

/** FR-02: 경력 조회는 USER 전용 메뉴 */
@Controller
@PreAuthorize("hasRole('USER')")
public class CareerController {

    @GetMapping("/career")
    public String career(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getUser().getName());
        model.addAttribute("hireDate", userDetails.getUser().getHireDate());
        model.addAttribute("homeUrl", "/home"); // USER 메인으로 돌아가기
        return "career";
    }
}
