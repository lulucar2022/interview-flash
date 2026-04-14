package com.flash.repository;

import com.flash.entity.WrongQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WrongQuestionRepository extends JpaRepository<WrongQuestion, Long> {
    
    /**
     * 查询用户的错题列表
     */
    List<WrongQuestion> findByUserIdOrderByLastWrongAtDesc(Long userId);
    
    /**
     * 查询指定用户的指定题目错题记录
     */
    Optional<WrongQuestion> findByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 检查题目是否在错题本中
     */
    boolean existsByUserIdAndQuestionId(Long userId, Long questionId);
    
    /**
     * 统计用户的错题数量
     */
    long countByUserId(Long userId);
    
    /**
     * 删除指定用户的指定题目错题记录
     */
    void deleteByUserIdAndQuestionId(Long userId, Long questionId);
}