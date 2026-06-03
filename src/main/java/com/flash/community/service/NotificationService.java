package com.flash.community.service;

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
        log.debug("createNotification: userId={}, type={}, fromUserId={}", userId, type, fromUserId);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setSummary(summary);
        notification.setFromUserId(fromUserId);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: id={}, userId={}, type={}", saved.getId(), userId, type);
        return saved;
    }
}
