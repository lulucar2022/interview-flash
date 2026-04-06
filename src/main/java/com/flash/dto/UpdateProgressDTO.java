package com.flash.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProgressDTO {
    
    @NotNull(message = "题目 ID 不能为空")
    private Long questionId;
    
    private Boolean isCorrect;
    
    private Boolean isFavorite;
    
    private String status;
}
