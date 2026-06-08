package com.flash.community.repository;

import com.flash.community.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByUserId(Long userId);
    List<Follow> findByFollowingId(Long followingId);
    Optional<Follow> findByUserIdAndFollowingId(Long userId, Long followingId);
    boolean existsByUserIdAndFollowingId(Long userId, Long followingId);
    long countByUserId(Long userId);
    long countByFollowingId(Long followingId);

    @Query(value = "SELECT DATE(f.created_at) AS d, COUNT(*) AS cnt FROM follows f WHERE f.following_id = :userId AND f.created_at >= :since GROUP BY DATE(f.created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> findFollowerDailyCount(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
