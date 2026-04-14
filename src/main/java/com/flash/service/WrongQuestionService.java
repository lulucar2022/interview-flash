package com.flash.service;

import com.flash.dto.WrongQuestionDTO;
import com.flash.entity.Question;
import com.flash.entity.WrongQuestion;
import com.flash.exception.BusinessException;
import com.flash.repository.QuestionRepository;
import com.flash.repository.WrongQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题本Service
 * 负责管理用户的错题记录
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongQuestionService {

    private final WrongQuestionRepository wrongQuestionRepository;
    private final QuestionRepository questionRepository;

    /**
     * 获取用户的错题列表
     */
    public List<WrongQuestionDTO> getUserWrongQuestions(Long userId) {
        return wrongQuestionRepository.findByUserIdOrderByLastWrongAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 添加错题记录
     * 如果已存在，则更新错误次数
     */
    @Transactional
    public void addWrongQuestion(Long userId, Long questionId, String userAnswer, Boolean isCorrect) {
        // 查询题目
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> BusinessException.notFound("题目不存在"));
        
        // 检查是否已存在错题记录
        var existingWrong = wrongQuestionRepository.findByUserIdAndQuestionId(userId, questionId);
        
        if (existingWrong.isPresent()) {
            // 已存在，更新错误次数
            WrongQuestion wrong = existingWrong.get();
            wrong.setWrongCount(wrong.getWrongCount() + 1);
            wrong.setUserAnswer(userAnswer);
            wrong.setIsCorrect(isCorrect);
            wrongQuestionRepository.save(wrong);
        } else {
            // 新建错题记录
            WrongQuestion wrong = new WrongQuestion();
            wrong.setUserId(userId);
            wrong.setQuestion(question);
            wrong.setUserAnswer(userAnswer);
            wrong.setCorrectAnswer(question.getAnswer());
            wrong.setIsCorrect(isCorrect != null ? isCorrect : false);
            wrong.setWrongCount(1);
            wrongQuestionRepository.save(wrong);
        }
    }

    /**
     * 从错题本移除
     */
    @Transactional
    public void removeWrongQuestion(Long userId, Long questionId) {
        wrongQuestionRepository.deleteByUserIdAndQuestionId(userId, questionId);
    }

    /**
     * 记录答题并自动处理错题本
     * 当isCorrect为false时自动添加到错题本
     */
    @Transactional
    public void recordAnswer(Long userId, Long questionId, String userAnswer, Boolean isCorrect) {
        if (isCorrect != null && !isCorrect) {
            addWrongQuestion(userId, questionId, userAnswer, isCorrect);
        } else if (isCorrect != null && isCorrect) {
            // 回答正确，从错题本移除
            removeWrongQuestion(userId, questionId);
        }
    }

    /**
     * 获取错题统计
     */
    public long getWrongQuestionCount(Long userId) {
        return wrongQuestionRepository.countByUserId(userId);
    }

    /**
     * 实体转DTO
     */
    private WrongQuestionDTO convertToDTO(WrongQuestion wrong) {
        WrongQuestionDTO dto = new WrongQuestionDTO();
        dto.setId(wrong.getId());
        dto.setUserId(wrong.getUserId());
        dto.setQuestionId(wrong.getQuestion().getId());
        dto.setQuestionTitle(wrong.getQuestion().getTitle());
        dto.setQuestionContent(wrong.getQuestion().getContent());
        dto.setCategoryName(wrong.getQuestion().getCategory().getName());
        dto.setType(wrong.getQuestion().getType() != null ? wrong.getQuestion().getType().name() : null);
        dto.setDifficulty(wrong.getQuestion().getDifficulty() != null ? wrong.getQuestion().getDifficulty().name() : null);
        dto.setUserAnswer(wrong.getUserAnswer());
        dto.setCorrectAnswer(wrong.getCorrectAnswer());
        dto.setIsCorrect(wrong.getIsCorrect());
        dto.setWrongCount(wrong.getWrongCount());
        dto.setCreatedAt(wrong.getCreatedAt());
        dto.setLastWrongAt(wrong.getLastWrongAt());
        return dto;
    }
}