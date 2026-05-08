package com.flash.repository;

import com.flash.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    Optional<UserProgress> findByQuestionIdAndUserId(Long questionId, Integer userId);
    
    List<UserProgress> findByUserId(Integer userId);
    
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.status = :status")
    List<UserProgress> findByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") UserProgress.Status status);
    
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.isFavorite = true")
    List<UserProgress> findFavoritesByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.isCorrect = false")
    List<UserProgress> findWrongQuestionsByUserId(@Param("userId") Integer userId);
    
    long countByUserIdAndStatus(Integer userId, UserProgress.Status status);
    
    long countByUserIdAndIsCorrect(Integer userId, Boolean isCorrect);

    @Query(value = """
        SELECT
            up.question_id,
            COUNT(*) AS total_attempts,
            COUNT(DISTINCT up.user_id) AS unique_users,
            COUNT(*) FILTER (WHERE up.last_reviewed_at >= CURRENT_DATE - 7) AS recent_attempts,
            COUNT(*) FILTER (WHERE up.is_correct = false) AS error_count
        FROM user_progress up
        GROUP BY up.question_id
        """, nativeQuery = true)
    List<Object[]> getHotQuestionStats();
}
