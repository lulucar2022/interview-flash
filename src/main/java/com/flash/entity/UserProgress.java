package com.flash.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户学习进度实体
 * 对应数据库表：user_progress
 * 记录每个用户对每个题目的学习状态
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_progress")
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联题目（多对一）
     * 每个进度记录对应一道题目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer userId;

    /**
     * 学习状态
     * NEW: 未学习
     * LEARNING: 学习中
     * MASTERED: 已掌握
     * REVIEW: 需复习
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 持久化前初始化
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // 初始化默认值，防止空指针
        if (reviewCount == null) reviewCount = 0;
        if (isFavorite == null) isFavorite = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 学习状态枚举
     */
    public enum Status {
        NEW,         // 未学习
        LEARNING,    // 学习中
        MASTERED,    // 已掌握
        REVIEW       // 需复习
    }
}
