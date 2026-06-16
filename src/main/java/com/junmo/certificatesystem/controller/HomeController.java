package com.junmo.certificatesystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.junmo.certificatesystem.common.enums.RoleType;
import com.junmo.certificatesystem.security.CustomUserDetails;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getUser().getName());
        model.addAttribute("role", userDetails.getUser().getRole().name());
        model.addAttribute("isAdmin", userDetails.getUser().getRole() == RoleType.ADMIN);
        return "home";
    }
}
