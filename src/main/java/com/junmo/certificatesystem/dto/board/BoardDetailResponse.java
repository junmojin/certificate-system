package com.junmo.certificatesystem.dto.board;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardDetailResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorName;
    private final String authorId;
    private final long viewCount;
    private final LocalDateTime createdAt;
    private final boolean hasAttachment;
    private final String originalFileName;
    private final Long fileSize;
}
