package com.flash.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProgressDTO {
    private Long id;
    private Long questionId;
    private String questionTitle;
    private Integer userId;
    private String status;
    private Boolean isCorrect;
    private Boolean isFavorite;
    private Integer reviewCount;
    private LocalDateTime lastReviewedAt;
}
