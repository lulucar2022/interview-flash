package com.flash.community.dto;

import com.flash.community.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String summary;
    private Boolean isRead;
    private Long fromUserId;
    private String fromUserNickname;
    private String fromUserAvatar;
    private Long targetId;
    private LocalDateTime createdAt;

    public static NotificationDTO from(Notification n, String nickname, String avatar) {
        return new NotificationDTO(
            n.getId(), n.getType(), n.getSummary(), n.getIsRead(),
            n.getFromUserId(), nickname, avatar,
            n.getTargetId(), n.getCreatedAt()
        );
    }
}
