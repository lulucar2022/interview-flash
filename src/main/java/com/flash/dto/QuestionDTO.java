package com.flash.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuestionDTO {
    private Long id;
    private String title;
    private String content;
    private String answer;
    private Long categoryId;
    private String categoryName;
    private String difficulty;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
