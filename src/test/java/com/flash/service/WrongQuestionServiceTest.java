package com.flash.service;

import com.flash.common.exception.BusinessException;
import com.flash.entity.Category;
import com.flash.entity.Question;
import com.flash.entity.WrongQuestion;
import com.flash.repository.QuestionRepository;
import com.flash.repository.WrongQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WrongQuestionServiceTest {

    @Mock
    private WrongQuestionRepository wrongQuestionRepository;
    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private WrongQuestionService wrongQuestionService;

    private Question buildQuestion(Long id) {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Java基础");
        Question q = new Question();
        q.setId(id);
        q.setTitle("Question " + id);
        q.setContent("Content");
        q.setAnswer("Answer");
        q.setCategory(cat);
        q.setType(Question.QuestionType.SHORT_ANSWER);
        q.setDifficulty(Question.Difficulty.MEDIUM);
        return q;
    }

    // ── addWrongQuestion ──

    @Test
    void addWrongQuestion_newRecord_createsEntry() {
        Question q = buildQuestion(1L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(wrongQuestionRepository.findByUserIdAndQuestionId(10L, 1L)).thenReturn(Optional.empty());
        when(wrongQuestionRepository.save(any(WrongQuestion.class))).thenAnswer(inv -> inv.getArgument(0));

        wrongQuestionService.addWrongQuestion(10L, 1L, "my wrong answer", false);

        verify(wrongQuestionRepository).save(argThat(w -> {
            assertEquals(10L, w.getUserId());
            assertEquals(1L, w.getQuestion().getId());
            assertEquals("my wrong answer", w.getUserAnswer());
            assertEquals("Answer", w.getCorrectAnswer());
            assertEquals(1, w.getWrongCount());
            return true;
        }));
    }

    @Test
    void addWrongQuestion_existingRecord_incrementsWrongCount() {
        Question q = buildQuestion(1L);
        WrongQuestion existing = new WrongQuestion();
        existing.setId(100L);
        existing.setUserId(10L);
        existing.setQuestion(q);
        existing.setWrongCount(2);
        existing.setUserAnswer("old answer");

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(wrongQuestionRepository.findByUserIdAndQuestionId(10L, 1L)).thenReturn(Optional.of(existing));
        when(wrongQuestionRepository.save(any(WrongQuestion.class))).thenReturn(existing);

        wrongQuestionService.addWrongQuestion(10L, 1L, "new wrong answer", false);

        assertEquals(3, existing.getWrongCount());
        assertEquals("new wrong answer", existing.getUserAnswer());
        verify(wrongQuestionRepository).save(existing);
    }

    @Test
    void addWrongQuestion_questionNotFound_throwsException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> wrongQuestionService.addWrongQuestion(10L, 99L, "answer", false));
        verify(wrongQuestionRepository, never()).save(any());
    }

    // ── recordAnswer ──

    @Test
    void recordAnswer_wrongAnswer_addsToWrongBook() {
        Question q = buildQuestion(1L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(wrongQuestionRepository.findByUserIdAndQuestionId(10L, 1L)).thenReturn(Optional.empty());
        when(wrongQuestionRepository.save(any(WrongQuestion.class))).thenAnswer(inv -> inv.getArgument(0));

        wrongQuestionService.recordAnswer(10L, 1L, "bad answer", false);

        verify(wrongQuestionRepository).save(any(WrongQuestion.class));
        verify(wrongQuestionRepository, never()).deleteByUserIdAndQuestionId(anyLong(), anyLong());
    }

    @Test
    void recordAnswer_correctAnswer_removesFromWrongBook() {
        wrongQuestionService.recordAnswer(10L, 1L, "good answer", true);

        verify(wrongQuestionRepository).deleteByUserIdAndQuestionId(10L, 1L);
        verify(wrongQuestionRepository, never()).save(any());
    }

    @Test
    void recordAnswer_nullIsCorrect_noAction() {
        wrongQuestionService.recordAnswer(10L, 1L, "answer", null);

        verify(wrongQuestionRepository, never()).save(any());
        verify(wrongQuestionRepository, never()).deleteByUserIdAndQuestionId(anyLong(), anyLong());
    }

    // ── removeWrongQuestion ──

    @Test
    void removeWrongQuestion_delegates() {
        wrongQuestionService.removeWrongQuestion(10L, 1L);
        verify(wrongQuestionRepository).deleteByUserIdAndQuestionId(10L, 1L);
    }

    // ── getWrongQuestionCount ──

    @Test
    void getWrongQuestionCount_delegates() {
        when(wrongQuestionRepository.countByUserId(10L)).thenReturn(5L);
        assertEquals(5L, wrongQuestionService.getWrongQuestionCount(10L));
    }
}
