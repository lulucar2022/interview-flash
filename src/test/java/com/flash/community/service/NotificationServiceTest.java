package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.dto.NotificationDTO;
import com.flash.community.entity.Notification;
import com.flash.community.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SseEmitterManager sseEmitterManager;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    // ── helpers ──

    private Notification buildNotification(Long id, Long userId, String type, Long fromUserId) {
        Notification n = new Notification();
        n.setId(id);
        n.setUserId(userId);
        n.setType(type);
        n.setSummary("test summary");
        n.setFromUserId(fromUserId);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.of(2025, 6, 12, 10, 0));
        return n;
    }

    private User buildUser(Long id, String nickname, String avatar) {
        User u = new User();
        u.setId(id);
        u.setNickname(nickname);
        u.setAvatarUrl(avatar);
        return u;
    }

    // ── getUserNotifications (N+1 fix) ──

    @Test
    void getUserNotifications_batchQuery_returnsDTOsWithSenderInfo() {
        Notification n1 = buildNotification(1L, 1L, "follow", 2L);
        Notification n2 = buildNotification(2L, 1L, "comment", 3L);
        Notification n3 = buildNotification(3L, 1L, "like", 2L);
        Page<Notification> page = new PageImpl<>(List.of(n1, n2, n3), PageRequest.of(0, 20), 3);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(page);

        User user2 = buildUser(2L, "Alice", "alice.jpg");
        User user3 = buildUser(3L, "Bob", "bob.jpg");
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of(user2, user3));

        Page<NotificationDTO> result = notificationService.getUserNotifications(1L, 0, 20);

        assertEquals(3, result.getContent().size());
        // fromUserId=2 appears twice, fromUserId=3 once — but only 2 distinct IDs queried
        verify(userRepository, times(1)).findAllById(anyCollection());
        verify(userRepository, never()).findById(anyLong());

        NotificationDTO dto1 = result.getContent().get(0);
        assertEquals("Alice", dto1.getFromUserNickname());
        assertEquals("alice.jpg", dto1.getFromUserAvatar());
    }

    @Test
    void getUserNotifications_nullFromUserId_returnsNullSender() {
        Notification n = buildNotification(1L, 1L, "system", null);
        Page<Notification> page = new PageImpl<>(List.of(n), PageRequest.of(0, 20), 1);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(page);

        Page<NotificationDTO> result = notificationService.getUserNotifications(1L, 0, 20);

        assertEquals(1, result.getContent().size());
        assertNull(result.getContent().get(0).getFromUserNickname());
        assertNull(result.getContent().get(0).getFromUserAvatar());
        verify(userRepository, never()).findAllById(anyCollection());
    }

    @Test
    void getUserNotifications_fromUserNotFound_returnsNullSender() {
        Notification n = buildNotification(1L, 1L, "follow", 999L);
        Page<Notification> page = new PageImpl<>(List.of(n), PageRequest.of(0, 20), 1);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(page);
        when(userRepository.findAllById(anyCollection())).thenReturn(List.of());

        Page<NotificationDTO> result = notificationService.getUserNotifications(1L, 0, 20);

        assertEquals(1, result.getContent().size());
        assertNull(result.getContent().get(0).getFromUserNickname());
    }

    @Test
    void getUserNotifications_emptyPage_skipsBatchQuery() {
        Page<Notification> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(page);

        Page<NotificationDTO> result = notificationService.getUserNotifications(1L, 0, 20);

        assertTrue(result.getContent().isEmpty());
        verify(userRepository, never()).findAllById(anyCollection());
    }

    // ── getUnreadCount ──

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);
        assertEquals(5L, notificationService.getUnreadCount(1L));
    }

    @Test
    void getUnreadCount_zeroWhenNone() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(0L);
        assertEquals(0L, notificationService.getUnreadCount(1L));
    }

    // ── markAsRead ──

    @Test
    void markAsRead_existingNotification_marksTrue() {
        Notification n = buildNotification(1L, 1L, "follow", 2L);
        n.setIsRead(false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(1L);

        assertTrue(n.getIsRead());
        verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_nonExisting_doesNothing() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.markAsRead(999L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_alreadyRead_stillSaves() {
        Notification n = buildNotification(1L, 1L, "follow", 2L);
        n.setIsRead(true);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(1L);

        assertTrue(n.getIsRead());
        verify(notificationRepository).save(n);
    }

    // ── markAllAsRead ──

    @Test
    void markAllAsRead_returnsAffectedCount() {
        when(notificationRepository.markAllAsRead(1L)).thenReturn(7);
        assertEquals(7, notificationService.markAllAsRead(1L));
    }

    @Test
    void markAllAsRead_zeroWhenNone() {
        when(notificationRepository.markAllAsRead(1L)).thenReturn(0);
        assertEquals(0, notificationService.markAllAsRead(1L));
    }

    // ── createNotification ──

    @Test
    void createNotification_withFromUser_savesAndSendsSSE() {
        User fromUser = buildUser(2L, "Alice", null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(fromUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });

        Notification result = notificationService.createNotification(1L, "follow", "关注了你", 2L, null);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("follow", result.getType());
        assertEquals("关注了你", result.getSummary());
        verify(notificationRepository).save(any(Notification.class));
        verify(sseEmitterManager).sendNotification(eq(1L), eq("follow"), eq("关注了你"), eq(2L), eq("Alice"), isNull());
    }

    @Test
    void createNotification_withoutFromUser_savesWithNullNickname() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(11L);
            return n;
        });

        Notification result = notificationService.createNotification(1L, "system", "系统通知", null, null);

        assertNotNull(result);
        verify(userRepository, never()).findById(anyLong());
        verify(sseEmitterManager).sendNotification(eq(1L), eq("system"), eq("系统通知"), isNull(), isNull(), isNull());
    }

    @Test
    void createNotification_withTargetId_passesToSSE() {
        User fromUser = buildUser(2L, "Bob", null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(fromUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.createNotification(1L, "comment", "评论了你的文章", 2L, 42L);

        verify(sseEmitterManager).sendNotification(eq(1L), eq("comment"), eq("评论了你的文章"), eq(2L), eq("Bob"), eq(42L));
    }

    @Test
    void createNotification_fromUserNotFound_sseWithNullNickname() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.createNotification(1L, "like", "点赞了你", 999L, 5L);

        verify(sseEmitterManager).sendNotification(eq(1L), eq("like"), eq("点赞了你"), eq(999L), isNull(), eq(5L));
    }
}
