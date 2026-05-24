package com.flash.community.entity;

import com.flash.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String tagName;

    @Column(length = 100)
    private String tagTitle;

    @Column(length = 500)
    private String tagDescription;

    @Column(nullable = false)
    private Integer articleCount = 0;
}
