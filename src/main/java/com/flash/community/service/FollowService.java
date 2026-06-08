package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Follow;
import com.flash.community.repository.FollowRepository;
import com.flash.common.exception.BusinessException;
import com.flash.community.dto.FollowUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
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
                            userId,
                            null
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

    public List<FollowUserDTO> getFollowersWithProfile(Long userId, Long currentUserId) {
        List<FollowUserDTO> followers = new ArrayList<>();
        List<Follow> byFollowingId = followRepository.findByFollowingId(userId);
        byFollowingId.forEach(follow -> {
            FollowUserDTO followUserDTO = new FollowUserDTO(null,null,null,null,false,false);
            Long userId1 = follow.getUserId();
            boolean isFollowing = followRepository.existsByUserIdAndFollowingId(currentUserId, userId1);
            boolean isMutual = isFollowing && followRepository.existsByUserIdAndFollowingId(userId1, currentUserId);
            User user = userRepository.findById(userId1)
                            .orElseThrow(() -> new BusinessException("用户不存在"));
            followUserDTO.setId(user.getId());
            followUserDTO.setNickname(user.getNickname());
            followUserDTO.setAvatarUrl(user.getAvatarUrl());
            followUserDTO.setBio(user.getBio());
            followUserDTO.setMutual(isMutual);
            followUserDTO.setFollowing(isFollowing);
            followers.add(followUserDTO);
        });
        return  followers;
    }

    public List<Map<String, Object>> getFollowerTrend(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days - 1).with(LocalTime.MIN);
        List<Object[]> rows = followRepository.findFollowerDailyCount(userId, since);

        Map<String, Long> dailyMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String date = row[0].toString();
            long count = ((Number) row[1]).longValue();
            dailyMap.put(date, count);
        }

        long baseCount = followRepository.countByFollowingId(userId);
        for (String d : dailyMap.keySet()) {
            baseCount -= dailyMap.get(d);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            String date = LocalDate.now().minusDays(days - 1 - i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            baseCount += dailyMap.getOrDefault(date, 0L);
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", date);
            entry.put("count", baseCount);
            result.add(entry);
        }
        return result;
    }

    public List<FollowUserDTO> getFollowingWithProfile(Long userId, Long currentUserId) {
        List<FollowUserDTO> followers = new ArrayList<>();
        List<Follow> byUserId = followRepository.findByUserId(userId);
        byUserId.forEach(follow -> {
            FollowUserDTO followUserDTO = new FollowUserDTO(null,null,null,null,false,false);
            Long followingId = follow.getFollowingId();
            boolean isFollowing = followRepository.existsByUserIdAndFollowingId(currentUserId, followingId);
            boolean isMutual = isFollowing && followRepository.existsByUserIdAndFollowingId(followingId, currentUserId);
            User user = userRepository.findById(followingId)
                            .orElseThrow(() -> new BusinessException("用户不存在"));
            followUserDTO.setId(user.getId());
            followUserDTO.setMutual(isMutual);
            followUserDTO.setFollowing(isFollowing);
            followUserDTO.setNickname(user.getNickname());
            followUserDTO.setAvatarUrl(user.getAvatarUrl());
            followUserDTO.setBio(user.getBio());
            followers.add(followUserDTO);
        });
        return   followers;
    }
}
