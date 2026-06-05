package com.flash.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wxl
 * @date 2026/6/5 14:34
 * @description
 */
@Data
@AllArgsConstructor
public class FollowUserDTO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private boolean isFollowing;
    private boolean isMutual;
    
}
