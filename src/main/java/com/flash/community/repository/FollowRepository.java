package com.flash.community.repository;

import com.flash.community.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByUserId(Long userId);
    List<Follow> findByFollowingId(Long followingId);
    Optional<Follow> findByUserIdAndFollowingId(Long userId, Long followingId);
    boolean existsByUserIdAndFollowingId(Long userId, Long followingId);
    long countByUserId(Long userId);
    long countByFollowingId(Long followingId);
}
