package com.junmo.certificatesystem.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.junmo.certificatesystem.dto.board.BoardWriteRequest;
import com.junmo.certificatesystem.security.CustomUserDetails;
import com.junmo.certificatesystem.service.board.BoardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 건의 게시판 — 사용자·관리자 공동 이용.
 * 글 작성은 누구나, 삭제는 작성자 또는 관리자만 가능.
 */
@Controller
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/boards")
    @Transactional(readOnly = true)
    public String boardList(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        addUserAttributes(model, userDetails);
        model.addAttribute("boards", boardService.getBoardList());
        return "board/list";
    }

    @GetMapping("/boards/new")
    public String writeForm(Authentication authentication, Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        addUserAttributes(model, userDetails);
        model.addAttribute("boardWriteRequest", new BoardWriteRequest());
        return "board/write";
    }

    @PostMapping("/boards")
    @Transactional
    public String createBoard(
            @Valid @ModelAttribute BoardWriteRequest boardWriteRequest,
            BindingResult bindingResult,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (bindingResult.hasErrors()) {
            addUserAttributes(model, userDetails);
            return "board/write";
        }

        try {
            Long boardId = boardService.createBoard(
                    userDetails.getUser(), boardWriteRequest, attachment);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 등록되었습니다.");
            return "redirect:/boards/" + boardId;
        } catch (IllegalArgumentException e) {
            addUserAttributes(model, userDetails);
            model.addAttribute("errorMessage", e.getMessage());
            return "board/write";
        }
    }

    @GetMapping("/boards/{id}")
    @Transactional
    public String boardDetail(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        addUserAttributes(model, userDetails);

        try {
            model.addAttribute("board", boardService.getBoardDetail(id));
            return "board/detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/boards";
        }
    }

    @GetMapping("/boards/{id}/attachment")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            return boardService.loadAttachment(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/boards/{id}/delete")
    @Transactional
    public String deleteBoard(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        try {
            boardService.deleteBoard(id, userDetails.getUsername(), isAdmin(userDetails));
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
            return "redirect:/boards";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/boards/" + id;
        }
    }

    private void addUserAttributes(Model model, CustomUserDetails userDetails) {
        boolean admin = isAdmin(userDetails);
        model.addAttribute("isAdmin", admin);
        model.addAttribute("homeUrl", admin ? "/admin/home" : "/home");
        model.addAttribute("name", userDetails.getUser().getName());
        model.addAttribute("userId", userDetails.getUsername());
        model.addAttribute("currentUserId", userDetails.getUsername());
        model.addAttribute("currentUserName", userDetails.getUser().getName());
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}
