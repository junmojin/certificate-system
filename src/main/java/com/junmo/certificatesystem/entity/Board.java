package com.junmo.certificatesystem.entity;

import com.junmo.certificatesystem.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "boards")
@Getter
@Setter
@NoArgsConstructor
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /** DB 호환용 고정 분류 (건의 게시판) */
    @Column(nullable = false, length = 20)
    private String category = "SUGGESTION";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private long viewCount = 0;

    @Column(length = 255)
    private String originalFileName;

    @Column(length = 255)
    private String storedFileName;

    private Long fileSize;

    @Column(nullable = false)
    private boolean deleted = false;

    public boolean hasAttachment() {
        return storedFileName != null && !storedFileName.isBlank();
    }
}
