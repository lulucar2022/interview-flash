package com.flash.community.dto;

import com.flash.community.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminArticleDTO {
    private Long id;
    private String title;
    private String authorName;
    private String topicName;
    private String status;
    private Integer viewCount;
    private Integer commentCount;
    private Integer thumbsUpCount;
    private LocalDateTime createdAt;

    public static AdminArticleDTO from(Article article) {
        return new AdminArticleDTO(
            article.getId(),
            article.getTitle(),
            article.getAuthor() != null ? article.getAuthor().getNickname() : null,
            article.getTopic() != null ? article.getTopic().getTopicName() : null,
            article.getStatus().name(),
            article.getViewCount(),
            article.getCommentCount(),
            article.getThumbsUpCount(),
            article.getCreatedAt()
        );
    }
}
