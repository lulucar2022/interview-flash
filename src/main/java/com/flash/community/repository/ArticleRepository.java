package com.flash.community.repository;

import com.flash.community.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByStatusOrderByCreatedAtDesc(Article.ArticleStatus status, Pageable pageable);

    Page<Article> findByAuthorIdAndStatus(Long authorId, Article.ArticleStatus status, Pageable pageable);

    Page<Article> findByTopicIdAndStatus(Long topicId, Article.ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.viewCount DESC")
    Page<Article> findHotArticles(Pageable pageable);

    @Query(value = "SELECT * FROM articles a WHERE a.status = 'PUBLISHED' AND "
         + "to_tsvector('simple', a.title || ' ' || a.content) @@ plainto_tsquery('simple', :keyword)",
         countQuery = "SELECT count(*) FROM articles a WHERE a.status = 'PUBLISHED' AND "
         + "to_tsvector('simple', a.title || ' ' || a.content) @@ plainto_tsquery('simple', :keyword)",
         nativeQuery = true)
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
