package com.flash.admin.controller;

import com.flash.common.dto.ApiResponse;
import com.flash.community.dto.AdminArticleDTO;
import com.flash.community.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<Page<AdminArticleDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(articleService.listAllForAdmin(page, size));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        articleService.deleteArticleForAdmin(id);
        return ApiResponse.success();
    }
}
