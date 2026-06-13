package com.flash.community.repository;

import com.flash.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticleIdOrderByCreatedAtAsc(Long articleId, Pageable pageable);
    long countByArticleId(Long articleId);

    List<Comment> findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(Long articleId);
    List<Comment> findByArticleIdAndParentIdIsNullOrderByCreatedAtDesc(Long articleId);
    List<Comment> findByArticleIdAndParentIdIsNotNull(Long articleId);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.likeCount = GREATEST(0, c.likeCount - 1) WHERE c.id = :id")
    void decrementLikeCount(@Param("id") Long id);
}
