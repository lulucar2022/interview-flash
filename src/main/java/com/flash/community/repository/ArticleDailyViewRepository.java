package com.flash.community.repository;

import com.flash.community.entity.ArticleDailyView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ArticleDailyViewRepository extends JpaRepository<ArticleDailyView, Long> {

    Optional<ArticleDailyView> findByUserIdAndDate(Long userId, LocalDate date);

    List<ArticleDailyView> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(adv.count), 0) FROM ArticleDailyView adv WHERE adv.userId = :userId AND adv.date BETWEEN :from AND :to")
    Long sumCountByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
