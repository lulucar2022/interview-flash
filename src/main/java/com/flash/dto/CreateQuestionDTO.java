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
    private String type;
    
    private String difficulty;
    
    /**
     * 题目选项（JSON格式）
     * 用于单选、多选、判断题
     * 格式: [{"label": "A", "content": "选项内容"}, ...]
     */
    private String options;
}
