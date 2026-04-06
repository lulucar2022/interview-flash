package com.flash.controller;

import com.flash.dto.CategoryDTO;
import com.flash.dto.CreateCategoryDTO;
import com.flash.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto.getName(), dto.getDescription()));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody CreateCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto.getName(), dto.getDescription()));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
