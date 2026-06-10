package com.flash.auth.dto;

import com.flash.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Boolean enabled;
    private String role;
    private LocalDateTime createdAt;

    public static AdminUserDTO from(User user) {
        return new AdminUserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getEnabled(),
            user.getRole() != null ? user.getRole().getCode() : "USER",
            user.getCreatedAt()
        );
    }
}
