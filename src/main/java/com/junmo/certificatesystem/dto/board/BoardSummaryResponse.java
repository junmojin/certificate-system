package com.junmo.certificatesystem.dto.board;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardSummaryResponse {

    private final Long id;
    private final String title;
    private final String authorName;
    private final String authorId;
    private final long viewCount;
    private final boolean hasAttachment;
    private final LocalDateTime createdAt;
}
