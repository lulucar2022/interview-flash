package com.flash.community.controller;

import com.flash.auth.jwt.CustomUserDetails;
import com.flash.common.dto.ApiResponse;
import com.flash.community.dto.CommentCreateRequest;
import com.flash.community.dto.CommentTreeDTO;
import com.flash.community.dto.CommentUpdateRequest;
import com.flash.community.entity.Comment;
import com.flash.community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ApiResponse<List<CommentTreeDTO>> list(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "oldest") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails != null ? userDetails.getId() : null;
        return ApiResponse.success(commentService.getArticleCommentsWithLikes(articleId, sort, currentUserId));
    }

    @PostMapping
    public ApiResponse<Comment> create(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            commentService.createComment(request.getContent(), articleId,
                userDetails.getId(), request.getParentId()));
    }

    @PutMapping("/{commentId}")
    public ApiResponse<Comment> update(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(
            commentService.updateComment(commentId, userDetails.getId(), request.getContent()));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> delete(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getId());
        return ApiResponse.success();
    }

    @PostMapping("/{commentId}/like")
    public ApiResponse<Map<String, Object>> like(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean liked = commentService.toggleLike(commentId, userDetails.getId());
        return ApiResponse.success(Map.of("liked", liked));
    }
}
