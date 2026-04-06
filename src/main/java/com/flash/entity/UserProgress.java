package com.flash.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_progress")
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Integer userId;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (reviewCount == null) reviewCount = 0;
        if (isFavorite == null) isFavorite = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        NEW,         // 未学习
        LEARNING,    // 学习中
        MASTERED,    // 已掌握
        REVIEW       // 需复习
    }
}
