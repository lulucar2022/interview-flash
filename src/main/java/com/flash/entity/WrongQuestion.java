package com.flash.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 错题本实体
 * 记录用户回答错误的题目
 * 对应数据库表：wrong_questions
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "wrong_questions")
public class WrongQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 关联题目（多对一）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_wrong_at", nullable = false)
    private LocalDateTime lastWrongAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastWrongAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastWrongAt = LocalDateTime.now();
    }
}