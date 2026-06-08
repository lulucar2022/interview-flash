package com.flash.community.service;

import com.flash.auth.entity.User;
import com.flash.auth.repository.UserRepository;
import com.flash.community.entity.Blacklist;
import com.flash.community.repository.BlacklistRepository;
import com.flash.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleBlock(Long blockerId, Long blockedId) {
        log.debug("toggleBlock: blockerId={}, blockedId={}", blockerId, blockedId);
        if (blockerId.equals(blockedId)) {
            throw new BusinessException("不能拉黑自己");
        }
        return blacklistRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .map(entry -> {
                    blacklistRepository.delete(entry);
                    log.info("Unblocked: blockerId={}, blockedId={}", blockerId, blockedId);
                    return false;
                })
                .orElseGet(() -> {
                    userRepository.findById(blockedId)
                            .orElseThrow(() -> new BusinessException("用户不存在"));
                    Blacklist bl = new Blacklist();
                    bl.setBlockerId(blockerId);
                    bl.setBlockedId(blockedId);
                    blacklistRepository.save(bl);
                    log.info("Blocked: blockerId={}, blockedId={}", blockerId, blockedId);
                    return true;
                });
    }

    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blacklistRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    public List<Long> getBlockedUserIds(Long userId) {
        return blacklistRepository.findBlockedUserIdsByBlockerId(userId);
    }

    public List<Blacklist> getBlockedEntries(Long userId) {
        return blacklistRepository.findByBlockerId(userId);
    }
}
