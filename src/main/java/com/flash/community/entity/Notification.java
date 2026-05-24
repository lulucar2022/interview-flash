package com.flash.community.entity;

import com.flash.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(nullable = false)
    private Boolean isRead = false;

    private Long fromUserId;

    private Long targetId;

    @Column(length = 200)
    private String summary;
}
