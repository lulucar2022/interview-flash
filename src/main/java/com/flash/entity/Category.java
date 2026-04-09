package com.flash.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类实体
 * 对应数据库表：categories
 * 一个分类包含多个题目（一对多关系）
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    /**
     * 关联题目列表（一对多）
     * mappedBy：表示Question表中的category字段维护关系
     * cascade：级联操作（当分类删除时，关联的题目也删除）
     * fetch：懒加载（访问时再加载关联数据）
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();
}
