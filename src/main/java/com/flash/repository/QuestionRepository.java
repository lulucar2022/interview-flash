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

    @Query(value = "SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("limit") int limit);

    @Query(value = "SELECT * FROM questions WHERE id NOT IN (SELECT question_id FROM user_progress WHERE user_id = :userId) ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomUnpracticedQuestions(@Param("userId") Integer userId, @Param("limit") int limit);

    @Query(value = "SELECT q.* FROM questions q JOIN user_progress up ON q.id = up.question_id WHERE up.user_id = :userId AND up.is_correct = false ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomWrongQuestions(@Param("userId") Integer userId, @Param("limit") int limit);
}
