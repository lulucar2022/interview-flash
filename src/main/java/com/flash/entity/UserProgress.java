package com.flash.entity;

import com.flash.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_progress")
public class UserProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    private Boolean isCorrect;

    private Boolean isFavorite;

    private Integer reviewCount;

    private LocalDateTime lastReviewedAt;

    public enum Status {
        NEW, LEARNING, MASTERED, REVIEW
    }
}
