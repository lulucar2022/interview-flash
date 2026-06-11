package com.flash.community.dto;

import com.flash.community.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论响应 DTO — 用于创建/更新评论的返回
 * 扁平化 author/article/parent 避免 JPA 懒加载序列化问题
 */
@Data
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private Long authorId;
    private String authorNickname;
    private String authorAvatarUrl;
    private Long articleId;
    private Long parentId;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentDTO from(Comment comment) {
        return new CommentDTO(
            comment.getId(),
            comment.getContent(),
            comment.getAuthor().getId(),
            comment.getAuthor().getNickname(),
            comment.getAuthor().getAvatarUrl(),
            comment.getArticle().getId(),
            comment.getParent() != null ? comment.getParent().getId() : null,
            comment.getLikeCount() != null ? comment.getLikeCount() : 0,
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
