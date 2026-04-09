package com.flash.service;

import com.flash.entity.Question;
import com.flash.entity.UserProgress;
import com.flash.exception.BusinessException;
import com.flash.repository.QuestionRepository;
import com.flash.repository.UserProgressRepository;
import com.flash.dto.UserProgressDTO;
import com.flash.dto.UpdateProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户学习进度Service
 * 负责管理用户的学习进度、错题本、收藏等功能
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final QuestionRepository questionRepository;

    /**
     * 获取用户所有学习进度
     * 
     * @param userId 用户ID
     * @return 进度DTO列表
     */
    public List<UserProgressDTO> getUserProgress(Integer userId) {
        return userProgressRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定题目的学习进度
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 进度DTO，如果不存在则返回null
     */
    public UserProgressDTO getProgressByQuestion(Integer userId, Long questionId) {
        return userProgressRepository.findByQuestionIdAndUserId(questionId, userId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * 获取用户的错题列表
     * 
     * @param userId 用户ID
     * @return 错题列表
     */
    public List<UserProgressDTO> getWrongQuestions(Integer userId) {
        return userProgressRepository.findWrongQuestionsByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户收藏列表
     * 
     * @param userId 用户ID
     * @return 收藏列表
     */
    public List<UserProgressDTO> getFavorites(Integer userId) {
        return userProgressRepository.findFavoritesByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户学习统计信息
     * 
     * @param userId 用户ID
     * @return 统计数据Map
     */
    public Map<String, Object> getStatistics(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各状态的数量
        long total = userProgressRepository.countByUserIdAndStatus(userId, UserProgress.Status.NEW) +
                     userProgressRepository.countByUserIdAndStatus(userId, UserProgress.Status.LEARNING) +
                     userProgressRepository.countByUserIdAndStatus(userId, UserProgress.Status.MASTERED) +
                     userProgressRepository.countByUserIdAndStatus(userId, UserProgress.Status.REVIEW);
        
        long mastered = userProgressRepository.countByUserIdAndStatus(userId, UserProgress.Status.MASTERED);
        long wrongCount = userProgressRepository.countByUserIdAndIsCorrect(userId, false);
        
        stats.put("totalQuestions", total);
        stats.put("masteredCount", mastered);
        stats.put("wrongCount", wrongCount);
        stats.put("progressRate", total > 0 ? String.format("%.2f", (double) mastered / total * 100) : "0.00");
        
        return stats;
    }

    /**
     * 更新用户学习进度
     * 核心业务逻辑：记录答题、收藏、状态变更
     * 
     * @param userId 用户ID
     * @param dto 更新参数
     * @return 更新后的进度DTO
     */
    @Transactional
    public UserProgressDTO updateProgress(Integer userId, UpdateProgressDTO dto) {
        // 1. 先查询题目是否存在
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        
        // 2. 查询该用户对此题目的进度记录
        // 如果不存在，则创建新的进度记录
        UserProgress progress = userProgressRepository.findByQuestionIdAndUserId(dto.getQuestionId(), userId)
                .orElse(new UserProgress());
        
        // 3. 如果是新建记录，初始化必要字段
        if (progress.getId() == null) {
            progress.setQuestion(question);
            progress.setUserId(userId);
            progress.setStatus(UserProgress.Status.LEARNING);
            progress.setReviewCount(0);
            progress.setIsFavorite(false);
        }
        
        // 4. 更新答题是否正确
        if (dto.getIsCorrect() != null) {
            progress.setIsCorrect(dto.getIsCorrect());
        }
        // 5. 更新收藏状态
        if (dto.getIsFavorite() != null) {
            progress.setIsFavorite(dto.getIsFavorite());
        }
        // 6. 更新学习状态
        if (dto.getStatus() != null) {
            progress.setStatus(UserProgress.Status.valueOf(dto.getStatus()));
        }
        
        // 7. 增加复习次数并记录最后复习时间
        // 使用null安全处理，防止空指针
        progress.setReviewCount((progress.getReviewCount() == null ? 0 : progress.getReviewCount()) + 1);
        progress.setLastReviewedAt(LocalDateTime.now());
        
        // 8. 保存并返回
        return convertToDTO(userProgressRepository.save(progress));
    }

    /**
     * 重置学习进度
     * 将指定题目的学习进度重置为初始状态
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     */
    @Transactional
    public void resetProgress(Integer userId, Long questionId) {
        UserProgress progress = userProgressRepository.findByQuestionIdAndUserId(questionId, userId)
                .orElseThrow(() -> BusinessException.notFound("学习记录不存在"));
        
        // 重置为未学习状态
        progress.setStatus(UserProgress.Status.NEW);
        progress.setIsCorrect(null);
        progress.setReviewCount(0);
        progress.setLastReviewedAt(null);
        
        userProgressRepository.save(progress);
    }

    /**
     * 实体转DTO
     */
    private UserProgressDTO convertToDTO(UserProgress progress) {
        UserProgressDTO dto = new UserProgressDTO();
        dto.setId(progress.getId());
        dto.setQuestionId(progress.getQuestion().getId());
        dto.setQuestionTitle(progress.getQuestion().getTitle());
        dto.setUserId(progress.getUserId());
        dto.setStatus(progress.getStatus().name());
        dto.setIsCorrect(progress.getIsCorrect());
        dto.setIsFavorite(progress.getIsFavorite());
        dto.setReviewCount(progress.getReviewCount());
        dto.setLastReviewedAt(progress.getLastReviewedAt());
        return dto;
    }
}
