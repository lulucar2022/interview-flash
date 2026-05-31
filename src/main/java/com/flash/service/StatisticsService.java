package com.flash.service;

import com.flash.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserProgressRepository userProgressRepository;

    /**
     * 获取指定天数内的每日答题统计
     */
    public List<Map<String, Object>> getDailyStats(Long userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> rows = userProgressRepository.findDailyStats(userId, since);

        // 构建完整日期序列（填充无数据的日期为 0）
        Map<String, Map<String, Object>> dateMap = new LinkedHashMap<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE);
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", date);
            entry.put("count", 0);
            entry.put("correct", 0);
            dateMap.put(date, entry);
        }

        for (Object[] row : rows) {
            String date = row[0].toString();
            long count = ((Number) row[1]).longValue();
            long correct = ((Number) row[2]).longValue();
            if (dateMap.containsKey(date)) {
                dateMap.get(date).put("count", count);
                dateMap.get(date).put("correct", correct);
            }
        }
        return new ArrayList<>(dateMap.values());
    }

    /**
     * 计算连续学习天数 + 最长连续记录
     */
    public Map<String, Object> getStreak(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusYears(1);
        List<Object[]> rows = userProgressRepository.findDailyStats(userId, since);

        // 收集有学习记录的所有日期
        Set<LocalDate> activeDates = new HashSet<>();
        for (Object[] row : rows) {
            activeDates.add(LocalDate.parse(row[0].toString()));
        }

        LocalDate today = LocalDate.now();
        int currentStreak = 0;
        int maxStreak = 0;
        int streak = 0;

        // 从今天往前数连续天数
        LocalDate d = today;
        while (activeDates.contains(d)) {
            currentStreak++;
            d = d.minusDays(1);
        }

        // 扫描全部得到最长连续记录
        LocalDate start = today.minusYears(1);
        for (d = start; !d.isAfter(today); d = d.plusDays(1)) {
            if (activeDates.contains(d)) {
                streak++;
                if (streak > maxStreak) maxStreak = streak;
            } else {
                streak = 0;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentStreak", currentStreak);
        result.put("maxStreak", maxStreak);
        return result;
    }

    /**
     * 获取各分类掌握度统计
     */
    public List<Map<String, Object>> getCategoryStats(Long userId) {
        List<Object[]> rows = userProgressRepository.findCategoryStats(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", row[0]);
            entry.put("total", ((Number) row[1]).longValue());
            entry.put("mastered", ((Number) row[2]).longValue());
            result.add(entry);
        }
        return result;
    }
}
