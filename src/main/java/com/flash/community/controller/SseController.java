package com.flash.community.controller;

import com.flash.auth.jwt.JwtTokenProvider;
import com.flash.community.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterManager sseEmitterManager;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam("token") String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            SseEmitter error = new SseEmitter(0L);
            try {
                error.send(SseEmitter.event().name("error").data("invalid token"));
                error.complete();
            } catch (Exception ignored) {}
            return error;
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        SseEmitter emitter = sseEmitterManager.register(userId);

        ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-heartbeat-" + userId);
            t.setDaemon(true);
            return t;
        });

        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data(""));
            } catch (Exception e) {
                log.debug("SSE heartbeat stopped for userId={}", userId);
                heartbeatExecutor.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);

        emitter.onCompletion(heartbeatExecutor::shutdown);
        emitter.onTimeout(heartbeatExecutor::shutdown);
        emitter.onError(e -> heartbeatExecutor.shutdown());

        return emitter;
    }
}
