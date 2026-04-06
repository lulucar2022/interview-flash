package com.flash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateQuestionDTO {
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    private String answer;
    
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    
    private String difficulty;
}
