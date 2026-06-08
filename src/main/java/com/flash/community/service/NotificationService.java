package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Notification;
import com.flash.community.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseEmitterManager sseEmitterManager;
    private final UserRepository userRepository;

    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    public Notification createNotification(Long userId, String type, String summary, Long fromUserId) {
        return createNotification(userId, type, summary, fromUserId, null);
    }

    public Notification createNotification(Long userId, String type, String summary, Long fromUserId, Long targetId) {
        log.debug("createNotification: userId={}, type={}, fromUserId={}, targetId={}", userId, type, fromUserId, targetId);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setSummary(summary);
        notification.setFromUserId(fromUserId);
        notification.setTargetId(targetId);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: id={}, userId={}, type={}", saved.getId(), userId, type);

        String fromUserNickname = null;
        if (fromUserId != null) {
            fromUserNickname = userRepository.findById(fromUserId).map(User::getNickname).orElse(null);
        }
        sseEmitterManager.sendNotification(userId, type, summary, fromUserId, fromUserNickname, targetId);

        return saved;
    }
}
