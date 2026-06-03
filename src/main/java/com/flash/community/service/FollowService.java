package com.flash.community.service;

import com.flash.community.entity.Follow;
import com.flash.community.repository.FollowRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;

    public boolean toggleFollow(Long userId, Long followingId) {
        log.debug("toggleFollow: userId={}, followingId={}", userId, followingId);
        if (userId.equals(followingId)) {
            log.warn("Cannot follow self: userId={}", userId);
            throw new BusinessException("不能关注自己");
        }
        return followRepository.findByUserIdAndFollowingId(userId, followingId)
                .map(follow -> {
                    followRepository.delete(follow);
                    log.info("Unfollowed: userId={}, followingId={}", userId, followingId);
                    return false;
                })
                .orElseGet(() -> {
                    Follow follow = new Follow();
                    follow.setUserId(userId);
                    follow.setFollowingId(followingId);
                    followRepository.save(follow);
                    log.info("Followed: userId={}, followingId={}", userId, followingId);
                    notificationService.createNotification(
                            followingId,
                            "follow",
                            "关注了你",
                            userId
                    );
                    return true;
                });
    }

    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId);
    }

    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByUserId(userId);
    }

    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    public long getFollowingCount(Long userId) {
        return followRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long followingId) {
        return followRepository.existsByUserIdAndFollowingId(userId, followingId);
    }
}
