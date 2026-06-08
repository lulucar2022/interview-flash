package com.flash.community.repository;

import com.flash.community.entity.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Optional<Blacklist> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("SELECT b.blockedId FROM Blacklist b WHERE b.blockerId = :userId")
    List<Long> findBlockedUserIdsByBlockerId(@Param("userId") Long userId);

    List<Blacklist> findByBlockerId(Long blockerId);
}
