package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Blacklist;
import com.flash.community.repository.BlacklistRepository;
import com.flash.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {

    @Mock
    private BlacklistRepository blacklistRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlacklistService blacklistService;

    @Test
    void toggleBlock_firstTime_returnsTrue() {
        when(blacklistRepository.findByBlockerIdAndBlockedId(1L, 2L)).thenReturn(Optional.empty());
        User user = new User();
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        boolean result = blacklistService.toggleBlock(1L, 2L);

        assertTrue(result);
        verify(blacklistRepository).save(any(Blacklist.class));
    }

    @Test
    void toggleBlock_alreadyBlocked_returnsFalse() {
        Blacklist existing = new Blacklist();
        existing.setBlockerId(1L);
        existing.setBlockedId(2L);
        when(blacklistRepository.findByBlockerIdAndBlockedId(1L, 2L)).thenReturn(Optional.of(existing));

        boolean result = blacklistService.toggleBlock(1L, 2L);

        assertFalse(result);
        verify(blacklistRepository).delete(existing);
    }

    @Test
    void toggleBlock_self_throwsException() {
        assertThrows(BusinessException.class, () -> blacklistService.toggleBlock(1L, 1L));
        verify(blacklistRepository, never()).findByBlockerIdAndBlockedId(any(), any());
    }

    @Test
    void toggleBlock_userNotExists_throwsException() {
        when(blacklistRepository.findByBlockerIdAndBlockedId(1L, 99L)).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> blacklistService.toggleBlock(1L, 99L));
    }

    @Test
    void isBlocked_exists_returnsTrue() {
        when(blacklistRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(true);

        assertTrue(blacklistService.isBlocked(1L, 2L));
    }

    @Test
    void isBlocked_notExists_returnsFalse() {
        when(blacklistRepository.existsByBlockerIdAndBlockedId(1L, 2L)).thenReturn(false);

        assertFalse(blacklistService.isBlocked(1L, 2L));
    }

    @Test
    void getBlockedUserIds_returnsList() {
        when(blacklistRepository.findBlockedUserIdsByBlockerId(1L)).thenReturn(List.of(2L, 3L));

        List<Long> ids = blacklistService.getBlockedUserIds(1L);

        assertEquals(2, ids.size());
        assertTrue(ids.contains(2L));
    }

    @Test
    void getBlockedUsersInfo_returnsInfo() {
        Blacklist entry = new Blacklist();
        entry.setId(1L);
        entry.setBlockerId(1L);
        entry.setBlockedId(2L);
        entry.setCreatedAt(LocalDateTime.now());
        when(blacklistRepository.findByBlockerId(1L)).thenReturn(List.of(entry));
        User blocked = new User();
        blocked.setId(2L);
        blocked.setNickname("BlockedUser");
        blocked.setAvatarUrl("http://avatar.url");
        when(userRepository.findById(2L)).thenReturn(Optional.of(blocked));

        List<Map<String, Object>> info = blacklistService.getBlockedUsersInfo(1L);

        assertEquals(1, info.size());
        assertEquals("BlockedUser", info.get(0).get("nickname"));
    }

    @Test
    void getBlockedUsersInfo_deletedUser_shows注销() {
        Blacklist entry = new Blacklist();
        entry.setId(1L);
        entry.setBlockerId(1L);
        entry.setBlockedId(99L);
        entry.setCreatedAt(LocalDateTime.now());
        when(blacklistRepository.findByBlockerId(1L)).thenReturn(List.of(entry));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        List<Map<String, Object>> info = blacklistService.getBlockedUsersInfo(1L);

        assertEquals(1, info.size());
        assertEquals("已注销", info.get(0).get("nickname"));
    }
}
