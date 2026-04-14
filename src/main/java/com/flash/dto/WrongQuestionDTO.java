package com.flash.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongQuestionDTO {
    private Long id;
    private Long userId;
    private Long questionId;
    private String questionTitle;
    private String questionContent;
    private String categoryName;
    private String type;
    private String difficulty;
    private String userAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer wrongCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastWrongAt;
}