package com.flash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryDTO {
    
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 100, message = "分类名称长度需要在1-100个字符之间")
    private String name;
    
    @Size(max = 500, message = "分类描述不能超过500个字符")
    private String description;
}
