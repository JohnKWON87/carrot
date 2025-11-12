package com.carrot.service;

import com.carrot.entity.Item;
import com.carrot.entity.User;
import com.carrot.constant.ModerationStatus;
import com.carrot.repository.ItemRepository;
import com.carrot.repository.UserRepository;
import com.carrot.repository.AdminLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 시스템 설정 및 통계 관련 서비스
 */
@Service
@Transactional(readOnly = true)
public class SystemSettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AdminLogRepository adminLogRepository;

    /**
     * 전체 시스템 통계 조회
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        // 사용자 통계
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByEnabled(true));
        stats.put("inactiveUsers", userRepository.countByEnabled(false));
        stats.put("adminUsers", userRepository.countByRole("ADMIN"));

        // 이번 달 신규 사용자
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        stats.put("newUsersThisMonth", userRepository.countNewUsersThisMonth(startOfMonth));

        // 상품 통계
        stats.put("totalItems", itemRepository.count());
        stats.put("visibleItems", itemRepository.countByModerationStatus(ModerationStatus.VISIBLE));
        stats.put("blindedItems", itemRepository.countByModerationStatus(ModerationStatus.BLINDED));
        stats.put("deletedItems", itemRepository.countByModerationStatus(ModerationStatus.DELETED));

        // 오늘 등록된 상품 수
        LocalDateTime startOfToday = LocalDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        stats.put("itemsAddedToday", itemRepository.countByCreatedAtBetween(
                startOfToday, LocalDateTime.now()));

        // 관리 로그 통계
        stats.put("totalAdminLogs", adminLogRepository.count());
        stats.put("logsThisMonth", adminLogRepository.countByModeratedAtAfter(startOfMonth));

        return stats;
    }

    /**
     * 카테고리별 상품 통계
     */
    public Map<String, Long> getCategoryStats() {
        Map<String, Long> categoryStats = new HashMap<>();
        String[] categories = {"전자제품", "의류", "도서", "가구", "기타"};

        for (String category : categories) {
            long count = itemRepository.countByCategoryAndModerationStatus(
                    category, ModerationStatus.VISIBLE);
            categoryStats.put(category, count);
        }

        return categoryStats;
    }

    /**
     * 최근 활동 내역 조회
     */
    public Map<String, Object> getRecentActivities() {
        Map<String, Object> activities = new HashMap<>();

        // 최근 가입한 사용자 5명
        List<User> recentUsers = userRepository.findAllByOrderByCreatedAtDesc(
                org.springframework.data.domain.PageRequest.of(0, 5)).getContent();
        activities.put("recentUsers", recentUsers);

        // 최근 등록된 상품 5개
        List<Item> recentItems = itemRepository.findRecentItems(
                ModerationStatus.VISIBLE,
                org.springframework.data.domain.PageRequest.of(0, 5));
        activities.put("recentItems", recentItems);

        return activities;
    }

    /**
     * 기간별 통계 (일주일)
     */
    public Map<String, Object> getWeeklyStats() {
        Map<String, Object> weeklyStats = new HashMap<>();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        long newUsers = userRepository.countNewUsersThisMonth(weekAgo);
        long newItems = itemRepository.countByCreatedAtBetween(weekAgo, LocalDateTime.now());

        weeklyStats.put("newUsersThisWeek", newUsers);
        weeklyStats.put("newItemsThisWeek", newItems);

        return weeklyStats;
    }

    /**
     * 데이터베이스 크기 정보 (간단한 추정)
     */
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> dbInfo = new HashMap<>();

        long totalRecords = userRepository.count() +
                itemRepository.count() +
                adminLogRepository.count();

        dbInfo.put("totalRecords", totalRecords);
        dbInfo.put("userRecords", userRepository.count());
        dbInfo.put("itemRecords", itemRepository.count());
        dbInfo.put("logRecords", adminLogRepository.count());

        return dbInfo;
    }
}