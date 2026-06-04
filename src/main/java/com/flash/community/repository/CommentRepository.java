package com.flash.community.repository;

import com.flash.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticleIdOrderByCreatedAtAsc(Long articleId, Pageable pageable);
    long countByArticleId(Long articleId);

    List<Comment> findByArticleIdAndParentIdIsNullOrderByCreatedAtAsc(Long articleId);
    List<Comment> findByArticleIdAndParentIdIsNullOrderByCreatedAtDesc(Long articleId);
    List<Comment> findByArticleIdAndParentIdIsNotNull(Long articleId);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
