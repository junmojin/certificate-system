package com.junmo.certificatesystem.service.audit;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class ActivityMenuResolver {

    private static final Pattern CERTIFICATE_VIEW = Pattern.compile("^/certificate/\\d+/view$");
    private static final Pattern BOARD_DETAIL = Pattern.compile("^/boards/\\d+$");
    private static final Pattern BOARD_DELETE = Pattern.compile("^/boards/\\d+/delete$");
    private static final Pattern CERTIFICATE_DETAIL = Pattern.compile("^/admin/certificates/\\d+$");
    private static final Pattern CERTIFICATE_APPROVE = Pattern.compile("^/admin/certificates/\\d+/approve$");
    private static final Pattern CERTIFICATE_REJECT = Pattern.compile("^/admin/certificates/\\d+/reject$");

    public Optional<String> resolve(String httpMethod, String requestUri) {
        if (httpMethod == null || requestUri == null) {
            return Optional.empty();
        }

        return switch (httpMethod) {
            case "GET" -> resolveGet(requestUri);
            case "POST" -> resolvePost(requestUri);
            default -> Optional.empty();
        };
    }

    private Optional<String> resolveGet(String uri) {
        return switch (uri) {
            case "/home" -> Optional.of("대시보드");
            case "/career" -> Optional.of("경력 조회");
            case "/certificate" -> Optional.of("증명서 신청");
            case "/boards" -> Optional.of("건의 게시판");
            case "/boards/new" -> Optional.of("건의 게시판 글쓰기");
            case "/admin/home" -> Optional.of("관리자 홈");
            case "/admin/certificates" -> Optional.of("증명서 신청 조회");
            default -> resolvePatternGet(uri);
        };
    }

    private Optional<String> resolvePatternGet(String uri) {
        if (CERTIFICATE_VIEW.matcher(uri).matches()) {
            return Optional.of("경력증명서 조회");
        }
        if (BOARD_DETAIL.matcher(uri).matches()) {
            return Optional.of("건의 게시판 상세");
        }
        if (CERTIFICATE_DETAIL.matcher(uri).matches()) {
            return Optional.of("증명서 신청 상세");
        }
        return Optional.empty();
    }

    private Optional<String> resolvePost(String uri) {
        if ("/certificate".equals(uri)) {
            return Optional.of("증명서 신청 등록");
        }
        if ("/boards".equals(uri)) {
            return Optional.of("건의 게시판 글 등록");
        }
        if (BOARD_DELETE.matcher(uri).matches()) {
            return Optional.of("건의 게시판 글 삭제");
        }
        if (CERTIFICATE_APPROVE.matcher(uri).matches()) {
            return Optional.of("증명서 발급 승인");
        }
        if (CERTIFICATE_REJECT.matcher(uri).matches()) {
            return Optional.of("증명서 발급 거부");
        }
        return Optional.empty();
    }
}
