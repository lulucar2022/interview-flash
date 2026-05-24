package com.flash.community.entity;

import com.flash.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "topics")
public class Topic extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String topicName;

    @Column(length = 500)
    private String topicIcon;

    @Column(length = 500)
    private String topicDescription;

    @Column(nullable = false)
    private Integer articleCount = 0;
}
