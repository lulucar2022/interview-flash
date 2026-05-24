package com.flash.community.repository;

import com.flash.community.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    List<ArticleTag> findByArticleId(Long articleId);
    List<ArticleTag> findByTagId(Long tagId);
    void deleteByArticleId(Long articleId);
}
