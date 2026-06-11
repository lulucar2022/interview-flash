package com.flash.community.dto;

import com.flash.auth.entity.User;
import com.flash.community.entity.Article;
import com.flash.community.entity.Series;
import com.flash.community.entity.Topic;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章响应 DTO — 用于所有公开/用户文章接口
 * 扁平化 author/topic/series 避免 JPA 懒加载序列化问题
 */
@Data
@AllArgsConstructor
public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private AuthorInfo author;
    private TopicInfo topic;
    private SeriesInfo series;
    private Integer seriesOrder;
    private Integer viewCount;
    private Integer commentCount;
    private Integer thumbsUpCount;
    private String status;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArticleDTO from(Article article) {
        User author = article.getAuthor();
        Topic topic = article.getTopic();
        Series series = article.getSeries();
        return new ArticleDTO(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            author != null ? new AuthorInfo(author.getId(), author.getNickname(), author.getAvatarUrl(), author.getUsername()) : null,
            topic != null ? new TopicInfo(topic.getId(), topic.getTopicName()) : null,
            series != null ? new SeriesInfo(series.getId(), series.getTitle()) : null,
            article.getSeriesOrder(),
            article.getViewCount(),
            article.getCommentCount(),
            article.getThumbsUpCount(),
            article.getStatus().name(),
            article.getTags(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }

    @Data
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatarUrl;
        private String username;
    }

    @Data
    @AllArgsConstructor
    public static class TopicInfo {
        private Long id;
        private String topicName;
    }

    @Data
    @AllArgsConstructor
    public static class SeriesInfo {
        private Long id;
        private String title;
    }
}
