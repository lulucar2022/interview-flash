package com.flash.community.service;

import com.flash.community.entity.Follow;
import com.flash.community.repository.FollowRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FollowService followService;

    @Test
    void toggleFollow_firstTime_returnsTrue() {
        when(followRepository.findByUserIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.empty());
        when(notificationService.createNotification(anyLong(), anyString(), anyString(), anyLong()))
                .thenReturn(null);

        boolean result = followService.toggleFollow(1L, 2L);

        assertTrue(result);
        verify(followRepository).save(any(Follow.class));
        verify(followRepository, never()).delete(any());
    }

    @Test
    void toggleFollow_alreadyFollowing_returnsFalse() {
        Follow existing = new Follow();
        existing.setUserId(1L);
        existing.setFollowingId(2L);
        when(followRepository.findByUserIdAndFollowingId(1L, 2L))
                .thenReturn(Optional.of(existing));

        boolean result = followService.toggleFollow(1L, 2L);

        assertFalse(result);
        verify(followRepository).delete(existing);
        verify(followRepository, never()).save(any());
    }

    @Test
    void toggleFollow_self_throwsException() {
        assertThrows(BusinessException.class,
                () -> followService.toggleFollow(1L, 1L));
        verify(followRepository, never()).findByUserIdAndFollowingId(any(), any());
    }

    @Test
    void isFollowing_exists_returnsTrue() {
        when(followRepository.existsByUserIdAndFollowingId(1L, 2L)).thenReturn(true);

        assertTrue(followService.isFollowing(1L, 2L));
    }

    @Test
    void isFollowing_notExists_returnsFalse() {
        when(followRepository.existsByUserIdAndFollowingId(1L, 2L)).thenReturn(false);

        assertFalse(followService.isFollowing(1L, 2L));
    }

    @Test
    void getFollowers_delegates() {
        followService.getFollowers(1L);
        verify(followRepository).findByFollowingId(1L);
    }

    @Test
    void getFollowing_delegates() {
        followService.getFollowing(1L);
        verify(followRepository).findByUserId(1L);
    }
}
