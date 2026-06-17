package com.junmo.certificatesystem.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * FR-02: 게시판은 USER + ADMIN 공통 메뉴.
 * hasAnyRole: 둘 중 하나의 권한이면 접근 가능.
 */
@Controller
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class BoardController {

    @GetMapping("/boards")
    public String boardList() {
        return "board/list";
    }
}
