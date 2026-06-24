package com.junmo.certificatesystem.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.board")
public class BoardFileProperties {

    /** 첨부파일 저장 디렉터리 (애플리케이션 실행 경로 기준) */
    private String uploadDir = "uploads/board";

    /** 허용 최대 파일 크기 (bytes) */
    private long maxFileSize = 5 * 1024 * 1024;
}
