package com.carrot.service;

import com.carrot.entity.AdminLog;
import com.carrot.entity.Item;
import com.carrot.repository.AdminLogRepository;
import com.carrot.repository.ItemRepository;
import com.carrot.constant.ModerationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 관리자 로그 관리를 위한 서비스 클래스
 */
@Service
@Transactional
public class AdminLogService {

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * 아이템 블라인드 처리 및 로그 저장
     */
    public AdminLog blindItem(Long itemId, String reason, String moderatorEmail) throws Exception {
        // 아이템 존재 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 아이템 상태를 BLINDED로 변경
        item.setModerationStatus(ModerationStatus.BLINDED);
        itemRepository.save(item);

        // 관리자 로그 생성 및 저장
        AdminLog adminLog = new AdminLog();
        adminLog.setItemId(itemId);
        adminLog.blindWithReason(reason, moderatorEmail);

        return adminLogRepository.save(adminLog);
    }

    /**
     * 아이템 삭제 처리 및 로그 저장
     */
    public AdminLog deleteItem(Long itemId, String reason, String moderatorEmail) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 아이템 상태를 DELETED로 변경
        item.setModerationStatus(ModerationStatus.DELETED);
        itemRepository.save(item);

        // 관리자 로그 생성 및 저장
        AdminLog adminLog = new AdminLog();
        adminLog.setItemId(itemId);
        adminLog.deleteWithReason(reason, moderatorEmail);

        return adminLogRepository.save(adminLog);
    }

    /**
     * 아이템 복원 처리 및 로그 저장
     */
    public AdminLog restoreItem(Long itemId, String moderatorEmail) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 아이템 상태를 VISIBLE로 복원
        item.setModerationStatus(ModerationStatus.VISIBLE);
        itemRepository.save(item);

        // 관리자 로그 생성 및 저장
        AdminLog adminLog = new AdminLog();
        adminLog.setItemId(itemId);
        adminLog.restore(moderatorEmail);

        return adminLogRepository.save(adminLog);
    }

    /**
     * 특정 아이템의 관리 이력 조회
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getItemModerationHistory(Long itemId) {
        return adminLogRepository.findByItemIdOrderByModeratedAtDesc(itemId);
    }

    /**
     * 특정 아이템의 현재 상태 조회
     */
    @Transactional(readOnly = true)
    public Optional<AdminLog> getCurrentItemStatus(Long itemId) {
        return adminLogRepository.findTopByItemIdOrderByModeratedAtDesc(itemId);
    }

    /**
     * 특정 관리자가 처리한 모든 로그 조회
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByModerator(String moderatorEmail) {
        return adminLogRepository.findByModeratorEmailOrderByModeratedAtDesc(moderatorEmail);
    }

    /**
     * 특정 상태의 모든 로그 조회
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByStatus(ModerationStatus status) {
        return adminLogRepository.findByModerationStatusOrderByModeratedAtDesc(status);
    }

    /**
     * 기간별 관리 로그 조회
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return adminLogRepository.findByModeratedAtBetweenOrderByModeratedAtDesc(startDate, endDate);
    }

    /**
     * 키워드로 관리 로그 검색
     */
    @Transactional(readOnly = true)
    public List<AdminLog> searchLogsByKeyword(String keyword) {
        return adminLogRepository.findByModerationReasonContainingIgnoreCaseOrderByModeratedAtDesc(keyword);
    }

    /**
     * 관리자별 처리 통계 조회
     */
    @Transactional(readOnly = true)
    public List<Object[]> getModerationStatsByModerator() {
        return adminLogRepository.findModerationStatsByModerator();
    }

    /**
     * 상태별 통계 조회
     */
    @Transactional(readOnly = true)
    public List<Object[]> getModerationStatsByStatus() {
        return adminLogRepository.findModerationStatsByStatus();
    }

    /**
     * 최근 N일간의 관리 로그 조회
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getRecentLogs(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return adminLogRepository.findRecentLogs(startDate);
    }

    /**
     * 특정 아이템이 블라인드 상태인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isItemBlocked(Long itemId) {
        return adminLogRepository.isItemBlocked(itemId);
    }

    /**
     * 자동 필터링으로 처리된 로그들 조회
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getAutoModerationLogs() {
        return adminLogRepository.findAutoModerationLogs();
    }

    /**
     * 모든 관리 로그 조회 (페이징 없이)
     */
    @Transactional(readOnly = true)
    public List<AdminLog> getAllLogs() {
        return adminLogRepository.findAll();
    }

    /**
     * 관리 로그 삭제 (관리자만 가능)
     */
    public void deleteLog(Long logId) throws Exception {
        AdminLog log = adminLogRepository.findById(logId)
                .orElseThrow(() -> new Exception("관리 로그를 찾을 수 없습니다."));

        adminLogRepository.delete(log);
    }

    /**
     * 대량 관리 로그 삭제 (오래된 로그 정리용)
     */
    public void deleteLogsOlderThan(LocalDateTime cutoffDate) {
        List<AdminLog> oldLogs = adminLogRepository.findAll();
        oldLogs.stream()
                .filter(log -> log.getCreatedAt().isBefore(cutoffDate))
                .forEach(adminLogRepository::delete);
    }
}