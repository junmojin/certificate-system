package com.junmo.certificatesystem.service.board;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.junmo.certificatesystem.config.properties.BoardFileProperties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게시판 첨부파일 저장/삭제.
 * 확장자·MIME·크기 검증 및 경로 탐색(path traversal)을 차단한다.
 */
@Service
@RequiredArgsConstructor
public class BoardFileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "png", "jpg", "jpeg", "gif", "txt", "doc", "docx", "xls", "xlsx", "hwp", "zip");

    private static final Map<String, Set<String>> EXTENSION_TO_MIME = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("gif", Set.of("image/gif")),
            Map.entry("txt", Set.of("text/plain")),
            Map.entry("doc", Set.of("application/msword")),
            Map.entry("docx", Set.of(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
            Map.entry("xls", Set.of("application/vnd.ms-excel")),
            Map.entry("xlsx", Set.of(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            Map.entry("hwp", Set.of(
                    "application/x-hwp", "application/haansofthwp", "application/octet-stream")),
            Map.entry("zip", Set.of("application/zip", "application/x-zip-compressed")));

    private final BoardFileProperties boardFileProperties;
    private final BoardInputSanitizer boardInputSanitizer;

    private Path uploadRoot;

    @PostConstruct
    void initUploadDirectory() throws IOException {
        uploadRoot = Path.of(boardFileProperties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public StoredBoardFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > boardFileProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("첨부파일은 5MB 이하만 업로드할 수 있습니다.");
        }

        String originalName = boardInputSanitizer.sanitizeOriginalFileName(
                StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename()));
        if (originalName.isBlank()) {
            throw new IllegalArgumentException("올바른 파일명이 아닙니다.");
        }

        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        validateContentType(file.getContentType(), extension);

        String storedName = UUID.randomUUID() + "." + extension;
        Path target = resolveStoredPath(storedName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("첨부파일 저장에 실패했습니다.");
        }

        return new StoredBoardFile(originalName, storedName, file.getSize());
    }

    public Path resolveStoredPath(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new IllegalArgumentException("파일 정보가 올바르지 않습니다.");
        }

        Path resolved = uploadRoot.resolve(storedFileName).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        return resolved;
    }

    public void deleteStoredFile(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveStoredPath(storedFileName));
        } catch (IllegalArgumentException | IOException ignored) {
            // 삭제 실패는 게시글 삭제 자체를 막지 않음
        }
    }

    private void validateContentType(String contentType, String extension) {
        if (contentType == null || contentType.isBlank()) {
            return;
        }
        Set<String> allowedMime = EXTENSION_TO_MIME.get(extension);
        if (allowedMime == null) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }
        String normalized = contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (!allowedMime.contains(normalized) && !"application/octet-stream".equals(normalized)) {
            throw new IllegalArgumentException("파일 형식이 확장자와 일치하지 않습니다.");
        }
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw new IllegalArgumentException("확장자가 있는 파일만 업로드할 수 있습니다.");
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    @Getter
    public static class StoredBoardFile {
        private final String originalFileName;
        private final String storedFileName;
        private final long fileSize;

        public StoredBoardFile(String originalFileName, String storedFileName, long fileSize) {
            this.originalFileName = originalFileName;
            this.storedFileName = storedFileName;
            this.fileSize = fileSize;
        }
    }
}
