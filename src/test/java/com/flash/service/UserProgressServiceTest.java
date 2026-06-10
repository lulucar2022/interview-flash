package com.flash.service;

import com.flash.common.exception.BusinessException;
import com.flash.dto.UpdateProgressDTO;
import com.flash.dto.UserProgressDTO;
import com.flash.entity.Category;
import com.flash.entity.Question;
import com.flash.entity.UserProgress;
import com.flash.entity.UserProgress.Status;
import com.flash.repository.QuestionRepository;
import com.flash.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private UserProgressService userProgressService;

    private Question buildQuestion(Long id, String title) {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Java基础");
        Question q = new Question();
        q.setId(id);
        q.setTitle(title);
        q.setContent("Content");
        q.setAnswer("Answer");
        q.setCategory(cat);
        q.setType(Question.QuestionType.SHORT_ANSWER);
        q.setDifficulty(Question.Difficulty.MEDIUM);
        return q;
    }

    // ── updateProgress ──

    @Test
    void updateProgress_newRecord_initializesWithLearning() {
        Question q = buildQuestion(1L, "HashMap");
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userProgressRepository.findByQuestionIdAndUserId(1L, 10L)).thenReturn(Optional.empty());
        when(userProgressRepository.save(any(UserProgress.class))).thenAnswer(inv -> {
            UserProgress up = inv.getArgument(0);
            up.setId(100L);
            return up;
        });

        UpdateProgressDTO dto = new UpdateProgressDTO();
        dto.setQuestionId(1L);
        dto.setIsCorrect(true);
        dto.setStatus("MASTERED");

        UserProgressDTO result = userProgressService.updateProgress(10L, dto);

        assertNotNull(result);
        assertEquals("MASTERED", result.getStatus());
        assertTrue(result.getIsCorrect());
        verify(userProgressRepository).save(argThat(up -> {
            assertEquals(Status.MASTERED, up.getStatus());
            assertEquals(10L, up.getUserId());
            assertEquals(1, up.getReviewCount());
            assertNotNull(up.getLastReviewedAt());
            return true;
        }));
    }

    @Test
    void updateProgress_existingRecord_updatesFields() {
        Question q = buildQuestion(1L, "HashMap");
        UserProgress existing = new UserProgress();
        existing.setId(100L);
        existing.setQuestion(q);
        existing.setUserId(10L);
        existing.setStatus(Status.LEARNING);
        existing.setIsCorrect(false);
        existing.setIsFavorite(false);
        existing.setReviewCount(3);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(q));
        when(userProgressRepository.findByQuestionIdAndUserId(1L, 10L)).thenReturn(Optional.of(existing));
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(existing);

        UpdateProgressDTO dto = new UpdateProgressDTO();
        dto.setQuestionId(1L);
        dto.setIsCorrect(true);
        dto.setIsFavorite(true);
        dto.setStatus("MASTERED");

        UserProgressDTO result = userProgressService.updateProgress(10L, dto);

        assertEquals(Status.MASTERED, existing.getStatus());
        assertTrue(existing.getIsCorrect());
        assertTrue(existing.getIsFavorite());
        assertEquals(4, existing.getReviewCount());
    }

    @Test
    void updateProgress_questionNotFound_throwsException() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateProgressDTO dto = new UpdateProgressDTO();
        dto.setQuestionId(99L);

        assertThrows(BusinessException.class, () -> userProgressService.updateProgress(10L, dto));
        verify(userProgressRepository, never()).save(any());
    }

    // ── getStatistics ──

    @Test
    void getStatistics_returnsCorrectCounts() {
        when(userProgressRepository.countByUserIdAndStatus(10L, Status.NEW)).thenReturn(2L);
        when(userProgressRepository.countByUserIdAndStatus(10L, Status.LEARNING)).thenReturn(3L);
        when(userProgressRepository.countByUserIdAndStatus(10L, Status.MASTERED)).thenReturn(5L);
        when(userProgressRepository.countByUserIdAndStatus(10L, Status.REVIEW)).thenReturn(0L);
        when(userProgressRepository.countByUserIdAndIsCorrect(10L, false)).thenReturn(1L);

        Map<String, Object> stats = userProgressService.getStatistics(10L);

        assertEquals(10L, stats.get("totalQuestions"));
        assertEquals(5L, stats.get("masteredCount"));
        assertEquals(1L, stats.get("wrongCount"));
        assertEquals("50.00", stats.get("progressRate"));
    }

    @Test
    void getStatistics_emptyProgress_returnsZeros() {
        when(userProgressRepository.countByUserIdAndStatus(eq(10L), any())).thenReturn(0L);
        when(userProgressRepository.countByUserIdAndIsCorrect(10L, false)).thenReturn(0L);

        Map<String, Object> stats = userProgressService.getStatistics(10L);

        assertEquals(0L, stats.get("totalQuestions"));
        assertEquals(0L, stats.get("masteredCount"));
        assertEquals("0.00", stats.get("progressRate"));
    }

    // ── resetProgress ──

    @Test
    void resetProgress_resetsToNew() {
        UserProgress progress = new UserProgress();
        progress.setId(100L);
        progress.setStatus(Status.MASTERED);
        progress.setIsCorrect(true);
        progress.setReviewCount(5);

        when(userProgressRepository.findByQuestionIdAndUserId(1L, 10L)).thenReturn(Optional.of(progress));
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(progress);

        userProgressService.resetProgress(10L, 1L);

        assertEquals(Status.NEW, progress.getStatus());
        assertNull(progress.getIsCorrect());
        assertEquals(0, progress.getReviewCount());
        assertNull(progress.getLastReviewedAt());
    }

    @Test
    void resetProgress_notFound_throwsException() {
        when(userProgressRepository.findByQuestionIdAndUserId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userProgressService.resetProgress(10L, 99L));
    }

    // ── getUserProgress ──

    @Test
    void getUserProgress_delegatesAndConverts() {
        Question q = buildQuestion(1L, "Test");
        UserProgress up = new UserProgress();
        up.setId(100L);
        up.setQuestion(q);
        up.setUserId(10L);
        up.setStatus(Status.LEARNING);
        up.setIsCorrect(true);
        up.setIsFavorite(false);
        up.setReviewCount(2);

        when(userProgressRepository.findByUserId(10L)).thenReturn(List.of(up));

        List<UserProgressDTO> result = userProgressService.getUserProgress(10L);

        assertEquals(1, result.size());
        assertEquals("LEARNING", result.get(0).getStatus());
        assertEquals(1L, result.get(0).getQuestionId());
    }
}
