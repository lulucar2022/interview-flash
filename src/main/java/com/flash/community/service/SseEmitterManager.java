package com.flash.community.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseEmitterManager {

    private final ObjectMapper objectMapper;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    private static final long TIMEOUT = 30 * 60 * 1000L;
    private static final int MAX_PER_USER = 10;

    public SseEmitter register(Long userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());

        if (emitters.size() >= MAX_PER_USER) {
            SseEmitter stale = emitters.remove(0);
            try {
                stale.completeWithError(new RuntimeException("connection limit exceeded"));
            } catch (Exception ignored) {}
        }

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.add(emitter);

        Runnable cleanup = () -> remove(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("{}"));
        } catch (IOException e) {
            remove(userId, emitter);
        }

        log.info("SSE registered: userId={}, active={}", userId, emitters.size());
        return emitter;
    }

    public void remove(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
        log.debug("SSE removed: userId={}", userId);
    }

    public void sendNotification(Long userId, String type, String summary,
            Long fromUserId, String fromUserNickname, String fromUserAvatar,
            Long notificationId, Long targetId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", notificationId);
            data.put("type", type);
            data.put("summary", summary);
            data.put("fromUserId", fromUserId);
            data.put("fromUserNickname", fromUserNickname != null ? fromUserNickname : "");
            data.put("fromUserAvatar", fromUserAvatar != null ? fromUserAvatar : "");
            data.put("targetId", targetId);
            data.put("createdAt", java.time.LocalDateTime.now().toString());
            String json = objectMapper.writeValueAsString(data);
            SseEmitter.SseEventBuilder event = SseEmitter.event().name("notification").data(json);

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(event);
                } catch (IOException e) {
                    log.warn("SSE send failed, removing: userId={}", userId);
                    remove(userId, emitter);
                }
            }
        } catch (Exception e) {
            log.error("SSE sendNotification error: userId={}", userId, e);
        }
    }
}
