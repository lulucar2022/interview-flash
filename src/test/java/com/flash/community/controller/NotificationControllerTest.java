package com.flash.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flash.auth.jwt.CustomUserDetails;
import com.flash.community.dto.NotificationDTO;
import com.flash.community.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        CustomUserDetails user = new CustomUserDetails(1L, "testuser", "USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    // ── helpers ──

    private NotificationDTO buildDTO(Long id, String type, boolean isRead, Long fromUserId) {
        return new NotificationDTO(
            id, type, "test summary", isRead,
            fromUserId, "Alice", "alice.jpg",
            42L, LocalDateTime.of(2025, 6, 12, 10, 0)
        );
    }

    // ── GET /api/notifications ──

    @Test
    void list_returnsPageOfDTOs() throws Exception {
        NotificationDTO dto1 = buildDTO(1L, "follow", false, 2L);
        NotificationDTO dto2 = buildDTO(2L, "comment", true, 3L);
        var page = new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 20), 2);

        when(notificationService.getUserNotifications(eq(1L), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/notifications").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].type").value("follow"))
            .andExpect(jsonPath("$.data.content[0].isRead").value(false))
            .andExpect(jsonPath("$.data.content[0].fromUserNickname").value("Alice"))
            .andExpect(jsonPath("$.data.content[1].type").value("comment"))
            .andExpect(jsonPath("$.data.content[1].isRead").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(notificationService).getUserNotifications(1L, 0, 20);
    }

    @Test
    void list_emptyPage_returnsEmptyContent() throws Exception {
        var page = new PageImpl<NotificationDTO>(List.of(), PageRequest.of(0, 20), 0);
        when(notificationService.getUserNotifications(eq(1L), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void list_customPagination_passesParams() throws Exception {
        var page = new PageImpl<NotificationDTO>(List.of(), PageRequest.of(2, 10), 0);
        when(notificationService.getUserNotifications(eq(1L), eq(2), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/notifications").param("page", "2").param("size", "10"))
            .andExpect(status().isOk());

        verify(notificationService).getUserNotifications(1L, 2, 10);
    }

    // ── GET /api/notifications/unread-count ──

    @Test
    void unreadCount_returnsCount() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    void unreadCount_zeroWhenNone() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(0L);

        mockMvc.perform(get("/api/notifications/unread-count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(0));
    }

    // ── PUT /api/notifications/{id}/read ──

    @Test
    void markRead_returnsSuccess() throws Exception {
        doNothing().when(notificationService).markAsRead(42L);

        mockMvc.perform(put("/api/notifications/42/read"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAsRead(42L);
    }

    @Test
    void markRead_differentId_passesCorrectParam() throws Exception {
        doNothing().when(notificationService).markAsRead(100L);

        mockMvc.perform(put("/api/notifications/100/read"))
            .andExpect(status().isOk());

        verify(notificationService).markAsRead(100L);
    }

    // ── PUT /api/notifications/read-all ──

    @Test
    void markAllRead_returnsSuccess() throws Exception {
        when(notificationService.markAllAsRead(1L)).thenReturn(7);

        mockMvc.perform(put("/api/notifications/read-all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(notificationService).markAllAsRead(1L);
    }

    @Test
    void markAllRead_zeroNotifications_stillSucceeds() throws Exception {
        when(notificationService.markAllAsRead(1L)).thenReturn(0);

        mockMvc.perform(put("/api/notifications/read-all"))
            .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(1L);
    }
}
