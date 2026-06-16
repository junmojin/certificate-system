package com.junmo.certificatesystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.junmo.certificatesystem.security.CustomUserDetails;

@Controller
public class CareerController {

    @GetMapping("/career")
    public String career(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("userId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getUser().getName());
        model.addAttribute("hireDate", userDetails.getUser().getHireDate());
        return "career";
    }
}
