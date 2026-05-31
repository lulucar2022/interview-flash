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
    
    Optional<UserProgress> findByQuestionIdAndUserId(Long questionId, Long userId);
    
    List<UserProgress> findByUserId(Long userId);
    
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.status = :status")
    List<UserProgress> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") UserProgress.Status status);
    
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.isFavorite = true")
    List<UserProgress> findFavoritesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT up FROM UserProgress up WHERE up.userId = :userId AND up.isCorrect = false")
    List<UserProgress> findWrongQuestionsByUserId(@Param("userId") Long userId);
    
    long countByUserIdAndStatus(Long userId, UserProgress.Status status);
    
    long countByUserIdAndIsCorrect(Long userId, Boolean isCorrect);

    /** 按日期聚合：每日答题数 + 正确数 */
    @Query("SELECT CAST(up.lastReviewedAt AS date), COUNT(up), " +
           "SUM(CASE WHEN up.isCorrect = true THEN 1 ELSE 0 END) " +
           "FROM UserProgress up " +
           "WHERE up.userId = :userId AND up.lastReviewedAt >= :since " +
           "GROUP BY CAST(up.lastReviewedAt AS date) " +
           "ORDER BY CAST(up.lastReviewedAt AS date)")
    List<Object[]> findDailyStats(@Param("userId") Long userId, @Param("since") java.time.LocalDateTime since);

    /** 按分类聚合：分类名 + 题目数 + 掌握数 */
    @Query("SELECT c.name, COUNT(up), " +
           "SUM(CASE WHEN up.status = 'MASTERED' THEN 1 ELSE 0 END) " +
           "FROM UserProgress up JOIN up.question q JOIN q.category c " +
           "WHERE up.userId = :userId " +
           "GROUP BY c.id, c.name")
    List<Object[]> findCategoryStats(@Param("userId") Long userId);

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
