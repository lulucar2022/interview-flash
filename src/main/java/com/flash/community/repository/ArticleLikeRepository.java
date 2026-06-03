package com.flash.community.repository;

import com.flash.community.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
    boolean existsByArticleIdAndUserId(Long articleId, Long userId);
    long countByArticleId(Long articleId);
    void deleteByArticleIdAndUserId(Long articleId, Long userId);
}
