package com.junmo.certificatesystem.service.board;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * 게시글 입력값 XSS 위험 완화.
 * HTML/스크립트 패턴을 제거하고 위험 문자열을 차단한다.
 */
@Component
public class BoardInputSanitizer {

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
    private static final Pattern DANGEROUS = Pattern.compile(
            "(?i)(<script|javascript:|vbscript:|data:text/html|on\\w+\\s*=)");

    public String sanitizeTitle(String title) {
        return sanitizePlainText(title, 200);
    }

    public String sanitizeContent(String content) {
        return sanitizePlainText(content, 5000);
    }

    public String sanitizeOriginalFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        String cleaned = fileName.strip()
                .replace("\\", "")
                .replace("/", "")
                .replace("..", "");
        if (cleaned.length() > 255) {
            cleaned = cleaned.substring(0, 255);
        }
        return cleaned;
    }

    private String sanitizePlainText(String value, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException("입력값이 비어 있습니다.");
        }

        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("입력값이 비어 있습니다.");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException("입력값이 너무 깁니다.");
        }
        if (trimmed.indexOf('\0') >= 0 || DANGEROUS.matcher(trimmed).find()) {
            throw new IllegalArgumentException("허용되지 않는 문자열이 포함되어 있습니다.");
        }

        return HTML_TAG.matcher(trimmed).replaceAll("");
    }
}
