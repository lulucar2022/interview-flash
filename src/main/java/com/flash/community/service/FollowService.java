package com.flash.community.service;

import com.flash.community.entity.Follow;
import com.flash.community.repository.FollowRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public boolean toggleFollow(Long userId, Long followingId) {
        if (userId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }
        return followRepository.findByUserIdAndFollowingId(userId, followingId)
                .map(follow -> {
                    followRepository.delete(follow);
                    return false;
                })
                .orElseGet(() -> {
                    Follow follow = new Follow();
                    follow.setUserId(userId);
                    follow.setFollowingId(followingId);
                    followRepository.save(follow);
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
}
