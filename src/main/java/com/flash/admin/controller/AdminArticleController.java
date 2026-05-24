package com.flash.admin.controller;

import com.flash.common.dto.ApiResponse;
import com.flash.common.exception.BusinessException;
import com.flash.community.entity.Article;
import com.flash.community.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleRepository articleRepository;

    @GetMapping
    public ApiResponse<Page<Article>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(articleRepository.findAll(PageRequest.of(page, size)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        articleRepository.delete(article);
        return ApiResponse.success();
    }
}
