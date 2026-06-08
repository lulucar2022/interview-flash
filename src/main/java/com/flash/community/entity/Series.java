package com.flash.community.entity;

import com.flash.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "series")
public class Series extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "article_count", nullable = false)
    private Integer articleCount = 0;
}
