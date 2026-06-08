package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.entity.Article;
import com.flash.community.entity.Series;
import com.flash.community.service.SeriesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @GetMapping
    public ApiResponse<List<Series>> list(
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ApiResponse.success(seriesService.getUserSeries(userId));
        }
        return ApiResponse.success(seriesService.getAllSeries());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Series series = seriesService.getSeries(id);
        Page<Article> articles = seriesService.getSeriesArticles(id, page, size);
        return ApiResponse.success(Map.of("series", series, "articles", articles));
    }

    @PostMapping
    public ApiResponse<Series> create(
            @RequestBody CreateSeriesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(seriesService.createSeries(
                userDetails.getId(), request.getTitle(), request.getDescription(), request.getCoverImage()));
    }

    @PutMapping("/{id}")
    public ApiResponse<Series> update(
            @PathVariable Long id,
            @RequestBody CreateSeriesRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(seriesService.updateSeries(
                id, userDetails.getId(), request.getTitle(), request.getDescription(), request.getCoverImage()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        seriesService.deleteSeries(id, userDetails.getId());
        return ApiResponse.success();
    }

    @PutMapping("/articles/{articleId}")
    public ApiResponse<Void> assignArticle(
            @PathVariable Long articleId,
            @RequestBody AssignArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        seriesService.assignArticle(articleId, userDetails.getId(), request.getSeriesId(), request.getOrder());
        return ApiResponse.success();
    }

    public static class CreateSeriesRequest {
        private String title;
        private String description;
        private String coverImage;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    }

    public static class AssignArticleRequest {
        private Long seriesId;
        private Integer order;

        public Long getSeriesId() { return seriesId; }
        public void setSeriesId(Long seriesId) { this.seriesId = seriesId; }
        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }
    }
}
