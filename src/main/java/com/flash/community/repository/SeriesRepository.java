package com.flash.community.repository;

import com.flash.community.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<Series, Long> {
    List<Series> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Series> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
