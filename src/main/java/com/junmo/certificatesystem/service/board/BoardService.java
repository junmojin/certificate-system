package com.junmo.certificatesystem.service.board;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.junmo.certificatesystem.dto.board.BoardDetailResponse;
import com.junmo.certificatesystem.dto.board.BoardSummaryResponse;
import com.junmo.certificatesystem.dto.board.BoardWriteRequest;
import com.junmo.certificatesystem.entity.Board;
import com.junmo.certificatesystem.entity.User;
import com.junmo.certificatesystem.repository.BoardRepository;
import com.junmo.certificatesystem.service.board.BoardFileStorageService.StoredBoardFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardInputSanitizer boardInputSanitizer;
    private final BoardFileStorageService boardFileStorageService;

    @Transactional(readOnly = true)
    public List<BoardSummaryResponse> getBoardList() {
        return boardRepository.findAllActive().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public BoardDetailResponse getBoardDetail(Long boardId) {
        Board board = getActiveBoard(boardId);
        board.setViewCount(board.getViewCount() + 1);
        return toDetail(board);
    }

    @Transactional
    public Long createBoard(User author, BoardWriteRequest request, MultipartFile attachment) {
        String title = boardInputSanitizer.sanitizeTitle(request.getTitle());
        String content = boardInputSanitizer.sanitizeContent(request.getContent());

        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setAuthor(author);

        StoredBoardFile storedFile = boardFileStorageService.store(attachment);
        if (storedFile != null) {
            board.setOriginalFileName(storedFile.getOriginalFileName());
            board.setStoredFileName(storedFile.getStoredFileName());
            board.setFileSize(storedFile.getFileSize());
        }

        return boardRepository.save(board).getId();
    }

    @Transactional
    public void deleteBoard(Long boardId, String userId, boolean isAdmin) {
        Board board = getActiveBoard(boardId);
        assertCanDelete(board, userId, isAdmin);
        board.setDeleted(true);
        boardFileStorageService.deleteStoredFile(board.getStoredFileName());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> loadAttachment(Long boardId) {
        Board board = getActiveBoard(boardId);
        if (!board.hasAttachment()) {
            throw new IllegalArgumentException("첨부파일이 없습니다.");
        }

        Path filePath = boardFileStorageService.resolveStoredPath(board.getStoredFileName());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("첨부파일을 찾을 수 없습니다.");
        }

        Resource resource = new FileSystemResource(filePath);
        String safeName = boardInputSanitizer.sanitizeOriginalFileName(board.getOriginalFileName());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(safeName, java.nio.charset.StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private Board getActiveBoard(Long boardId) {
        return boardRepository.findActiveById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    private void assertCanDelete(Board board, String userId, boolean isAdmin) {
        if (isAdmin || board.getAuthor().getUserId().equals(userId)) {
            return;
        }
        throw new IllegalArgumentException("삭제 권한이 없습니다.");
    }

    private BoardSummaryResponse toSummary(Board board) {
        return BoardSummaryResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .authorName(board.getAuthor().getName())
                .authorId(board.getAuthor().getUserId())
                .viewCount(board.getViewCount())
                .hasAttachment(board.hasAttachment())
                .createdAt(board.getCreatedAt())
                .build();
    }

    private BoardDetailResponse toDetail(Board board) {
        return BoardDetailResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorName(board.getAuthor().getName())
                .authorId(board.getAuthor().getUserId())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .hasAttachment(board.hasAttachment())
                .originalFileName(board.getOriginalFileName())
                .fileSize(board.getFileSize())
                .build();
    }
}
