package com.flash.repository;

import com.flash.entity.Question;
import com.flash.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    Page<Question> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Question> findByDifficulty(Question.Difficulty difficulty, Pageable pageable);
    
    Page<Question> findByCategoryIdAndDifficulty(Long categoryId, Question.Difficulty difficulty, Pageable pageable);
    
    @Query("SELECT q FROM Question q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    List<Question> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    
    long countByCategoryId(Long categoryId);

    @Query("SELECT q FROM Question q WHERE " +
            "(:categoryId IS NULL OR q.category.id = :categoryId) AND " +
            "(:type IS NULL OR q.type = :type) AND " +
            "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    List<Question> filterQuestions(@Param("categoryId") Long categoryId,
                                  @Param("type") Question.QuestionType type,
                                  @Param("difficulty") Question.Difficulty difficulty);
}
