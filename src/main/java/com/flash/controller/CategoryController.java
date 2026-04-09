package com.flash.controller;

import com.flash.dto.ApiResponse;
import com.flash.dto.CategoryDTO;
import com.flash.dto.CreateCategoryDTO;
import com.flash.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "题目分类相关接口")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public ApiResponse<List<CategoryDTO>> getAllCategories() {
        return ApiResponse.success(categoryService.getAllCategories());
    }

    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public ApiResponse<CategoryDTO> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        return ApiResponse.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public ApiResponse<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryDTO dto) {
        return ApiResponse.success("创建成功", categoryService.createCategory(dto.getName(), dto.getDescription()));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ApiResponse<CategoryDTO> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody CreateCategoryDTO dto) {
        return ApiResponse.success("更新成功", categoryService.updateCategory(id, dto.getName(), dto.getDescription()));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success("删除成功", null);
    }
}
