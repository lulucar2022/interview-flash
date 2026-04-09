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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final QuestionRepository questionRepository;

    public List<UserProgressDTO> getUserProgress(Integer userId) {
        return userProgressRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserProgressDTO getProgressByQuestion(Integer userId, Long questionId) {
        return userProgressRepository.findByQuestionIdAndUserId(questionId, userId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<UserProgressDTO> getWrongQuestions(Integer userId) {
        return userProgressRepository.findWrongQuestionsByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserProgressDTO> getFavorites(Integer userId) {
        return userProgressRepository.findFavoritesByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics(Integer userId) {
        Map<String, Object> stats = new HashMap<>();
        
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

    @Transactional
    public UserProgressDTO updateProgress(Integer userId, UpdateProgressDTO dto) {
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        
        UserProgress progress = userProgressRepository.findByQuestionIdAndUserId(dto.getQuestionId(), userId)
                .orElse(new UserProgress());
        
        if (progress.getId() == null) {
            progress.setQuestion(question);
            progress.setUserId(userId);
            progress.setStatus(UserProgress.Status.LEARNING);
            progress.setReviewCount(0);
            progress.setIsFavorite(false);
        }
        
        if (dto.getIsCorrect() != null) {
            progress.setIsCorrect(dto.getIsCorrect());
        }
        if (dto.getIsFavorite() != null) {
            progress.setIsFavorite(dto.getIsFavorite());
        }
        if (dto.getStatus() != null) {
            progress.setStatus(UserProgress.Status.valueOf(dto.getStatus()));
        }
        
        progress.setReviewCount((progress.getReviewCount() == null ? 0 : progress.getReviewCount()) + 1);
        progress.setLastReviewedAt(LocalDateTime.now());
        
        return convertToDTO(userProgressRepository.save(progress));
    }

    @Transactional
    public void resetProgress(Integer userId, Long questionId) {
        UserProgress progress = userProgressRepository.findByQuestionIdAndUserId(questionId, userId)
                .orElseThrow(() -> BusinessException.notFound("学习记录不存在"));
        
        progress.setStatus(UserProgress.Status.NEW);
        progress.setIsCorrect(null);
        progress.setReviewCount(0);
        progress.setLastReviewedAt(null);
        
        userProgressRepository.save(progress);
    }

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
