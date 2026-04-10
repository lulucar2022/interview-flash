package com.flash.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目实体
 * 对应数据库表：questions
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    /**
     * 关联分类（多对一）
     * 每个题目属于一个分类
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * 题目类型
     * SINGLE_CHOICE: 单选题
     * MULTIPLE_CHOICE: 多选题
     * TRUE_FALSE: 判断题
     * FILL_BLANK: 填空题
     * SHORT_ANSWER: 简答题
     * CODING: 编程题
     * SCENARIO: 情景分析题
     */
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    /**
     * 关联用户进度（一对多）
     * 一个题目可以有多个用户的学习进度记录
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProgress> userProgresses = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (type == null) {
            type = QuestionType.SINGLE_CHOICE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 题目类型枚举
     */
    public enum QuestionType {
        SINGLE_CHOICE,    // 单选题
        MULTIPLE_CHOICE,  // 多选题
        TRUE_FALSE,       // 判断题
        FILL_BLANK,       // 填空题
        SHORT_ANSWER,     // 简答题
        CODING,           // 编程题
        SCENARIO          // 情景分析题
    }

    /**
     * 题目难度枚举
     */
    public enum Difficulty {
        EASY,      // 简单
        MEDIUM,    // 中等
        HARD       // 困难
    }
}
