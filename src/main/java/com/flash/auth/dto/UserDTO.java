package com.flash.auth.dto;

import com.flash.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String role;

    public static UserDTO from(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getBio(),
            user.getRole() != null ? user.getRole().getCode() : "USER"
        );
    }
}
