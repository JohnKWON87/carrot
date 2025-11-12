package com.carrot.service;

import com.carrot.entity.AdminLog;
import com.carrot.entity.Item;
import com.carrot.entity.User;
import com.carrot.repository.ItemRepository;
import com.carrot.constant.ItemSellStatus;
import com.carrot.constant.ModerationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 상품 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AdminService adminService;

    // ===== 상품 등록/수정/삭제 =====

    /**
     * 상품 등록
     */
    public Item registerItem(Item item) throws Exception {
        try {
            // 입력 검증
            validateItem(item);

            // 일단 저장
            Item savedItem = itemRepository.save(item);

            // 자동 필터링 검사 (부적절한 단어 체크)
            adminService.autoModerateContent(
                    savedItem.getId(),
                    savedItem.getTitle(),
                    savedItem.getDescription(),
                    "system@carrot.com"
            );

            return savedItem;

        } catch (Exception e) {
            throw new Exception("상품 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품 정보 수정
     */
    public Item updateItem(Long itemId, Item updatedItem, User currentUser) throws Exception {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 수정 권한 확인 (본인 또는 관리자만)
        if (!existingItem.getSeller().getId().equals(currentUser.getId()) &&
                !currentUser.isAdmin()) {
            throw new Exception("상품을 수정할 권한이 없습니다.");
        }

        // 수정 가능한 필드만 업데이트
        existingItem.setTitle(updatedItem.getTitle());
        existingItem.setDescription(updatedItem.getDescription());
        existingItem.setPrice(updatedItem.getPrice());
        existingItem.setCategory(updatedItem.getCategory());
        existingItem.setLocation(updatedItem.getLocation());

        if (updatedItem.getImageUrl() != null) {
            existingItem.setImageUrl(updatedItem.getImageUrl());
        }

        // 입력 검증
        validateItem(existingItem);

        // 자동 필터링 재검사
        adminService.autoModerateContent(
                existingItem.getId(),
                existingItem.getTitle(),
                existingItem.getDescription(),
                "system@carrot.com"
        );

        return itemRepository.save(existingItem);
    }

    /**
     * 상품 삭제
     */
    public void deleteItem(Long itemId, User currentUser) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 삭제 권한 확인
        if (!item.getSeller().getId().equals(currentUser.getId()) &&
                !currentUser.isAdmin()) {
            throw new Exception("상품을 삭제할 권한이 없습니다.");
        }

        itemRepository.delete(item);
    }

    // ===== 상품 조회 =====

    /**
     * 상품 상세 조회 (조회수 증가)
     */
    public Item getItemDetail(Long itemId) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 숨겨진 상품은 관리자만 볼 수 있음
        if (item.isBlocked()) {
            throw new Exception("조회할 수 없는 상품입니다.");
        }

        // 조회수 증가
        item.incrementViewCount();
        itemRepository.save(item);

        return item;
    }

    /**
     * 모든 판매 중인 상품 조회
     */
    @Transactional(readOnly = true)
    public List<Item> getAllAvailableItems() {
        return itemRepository.findByModerationStatusOrderByCreatedAtDesc(ModerationStatus.VISIBLE);
    }

    /**
     * 페이징된 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Item> getItemsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemRepository.findByModerationStatusOrderByCreatedAtDesc(
                ModerationStatus.VISIBLE, pageable);
    }

    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    public List<Item> getItemsByCategory(String category) {
        return itemRepository.findByCategoryAndModerationStatusOrderByCreatedAtDesc(
                category, ModerationStatus.VISIBLE);
    }

    /**
     * 특정 사용자의 등록 상품 조회
     */
    @Transactional(readOnly = true)
    public List<Item> getItemsBySeller(User seller) {
        return itemRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    /**
     * 최근 등록된 상품들 조회 (메인 페이지용)
     */
    @Transactional(readOnly = true)
    public List<Item> getRecentItems(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return itemRepository.findRecentItems(ModerationStatus.VISIBLE, pageable);
    }

    // ===== 검색 기능 =====

    /**
     * 키워드로 상품 검색
     */
    @Transactional(readOnly = true)
    public List<Item> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAvailableItems();
        }
        return itemRepository.findByTitleOrDescriptionContaining(
                keyword.trim(), ModerationStatus.VISIBLE);
    }

    /**
     * 카테고리와 키워드로 검색
     */
    @Transactional(readOnly = true)
    public List<Item> searchItemsByCategory(String keyword, String category) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getItemsByCategory(category);
        }
        return itemRepository.findByKeywordAndCategory(
                keyword.trim(), category, ModerationStatus.VISIBLE);
    }

    /**
     * 복합 검색 (키워드, 카테고리, 정렬)
     */
    @Transactional(readOnly = true)
    public List<Item> searchItemsWithFilters(String keyword, String category, String sort) {
        List<Item> items;

        // 1. 기본 검색
        if ("all".equals(category)) {
            items = searchItems(keyword);
        } else {
            items = searchItemsByCategory(keyword, category);
        }

        // 2. 정렬 적용
        return applySorting(items, sort);
    }

    /**
     * 가격 범위로 검색
     */
    @Transactional(readOnly = true)
    public List<Item> searchItemsByPriceRange(Integer minPrice, Integer maxPrice) {
        if (minPrice == null) minPrice = 0;
        if (maxPrice == null) maxPrice = Integer.MAX_VALUE;

        return itemRepository.findByPriceBetween(minPrice, maxPrice, ModerationStatus.VISIBLE);
    }

    // ===== 상품 상태 관리 =====

    /**
     * 상품 상태 변경
     */
    public Item changeItemStatus(Long itemId, ItemSellStatus newStatus, User currentUser) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        // 권한 확인
        if (!item.getSeller().getId().equals(currentUser.getId())) {
            throw new Exception("상품 상태를 변경할 권한이 없습니다.");
        }

        item.setSellStatus(newStatus);
        return itemRepository.save(item);
    }

    // ===== 통계 및 관리 =====

    /**
     * 카테고리별 상품 수 조회
     */
    @Transactional(readOnly = true)
    public long getItemCountByCategory(String category) {
        return itemRepository.countByCategoryAndModerationStatus(category, ModerationStatus.VISIBLE);
    }

    /**
     * 전체 상품 수 조회
     */
    @Transactional(readOnly = true)
    public long getTotalItemCount() {
        return itemRepository.countByModerationStatus(ModerationStatus.VISIBLE);
    }

    // ===== 내부 헬퍼 메서드 =====

    /**
     * 상품 입력값 검증
     */
    private void validateItem(Item item) throws Exception {
        if (item.getTitle() == null || item.getTitle().trim().length() < 5) {
            throw new Exception("상품 제목은 5글자 이상이어야 합니다.");
        }

        if (item.getTitle().length() > 100) {
            throw new Exception("상품 제목은 100글자를 초과할 수 없습니다.");
        }

        if (item.getDescription() == null || item.getDescription().trim().length() < 10) {
            throw new Exception("상품 설명은 10글자 이상이어야 합니다.");
        }

        if (item.getDescription().length() > 2000) {
            throw new Exception("상품 설명은 2000글자를 초과할 수 없습니다.");
        }

        if (item.getPrice() == null || item.getPrice() < 0) {
            throw new Exception("올바른 가격을 입력해주세요.");
        }

        if (item.getPrice() > 999999999) {
            throw new Exception("가격이 너무 높습니다.");
        }

        if (item.getCategory() == null || item.getCategory().trim().isEmpty()) {
            throw new Exception("카테고리를 선택해주세요.");
        }

        if (item.getLocation() == null || item.getLocation().trim().isEmpty()) {
            throw new Exception("거래 지역을 입력해주세요.");
        }
    }

    /**
     * 검색 결과 정렬 적용
     */
    private List<Item> applySorting(List<Item> items, String sort) {
        if ("price_low".equals(sort)) {
            items.sort((a, b) -> Integer.compare(a.getPrice(), b.getPrice()));
        } else if ("price_high".equals(sort)) {
            items.sort((a, b) -> Integer.compare(b.getPrice(), a.getPrice()));
        } else if ("view_count".equals(sort)) {
            items.sort((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()));
        } else if ("wish_count".equals(sort)) {
            items.sort((a, b) -> Integer.compare(b.getWishCount(), a.getWishCount()));
        }
        // "latest"는 기본 정렬이므로 별도 처리 없음

        return items;
    }
    // ItemService.java에 추가해야 할 메서드들

    // 1. 관리자용 모든 아이템 조회 (상태 무관)
    @Transactional(readOnly = true)
    public List<Item> getAllItemsForAdmin() {
        return itemRepository.findAllByOrderByCreatedAtDesc();
    }

    // 2. 관리자용 페이징된 아이템 조회
    @Transactional(readOnly = true)
    public Page<Item> getItemsForAdminWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemRepository.findAllByOrderByCreatedAtDesc(pageable); // Repository에 추가 필요
    }

    // 3. 상태별 아이템 조회
    @Transactional(readOnly = true)
    public List<Item> getItemsByModerationStatus(ModerationStatus status) {
        return itemRepository.findByModerationStatusOrderByCreatedAtDesc(status);
    }

    // 4. 아이템 관리 상태 변경 (관리자용)
    public Item moderateItem(Long itemId, ModerationStatus newStatus, String reason, String moderatorEmail) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new Exception("상품을 찾을 수 없습니다."));

        item.setModerationStatus(newStatus);

        // AdminLog는 AdminService나 AdminLogService에서 처리하므로 여기서는 제거
        // 단순히 아이템 상태만 변경
        return itemRepository.save(item);
    }

    // 5. 아이템 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getItemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", itemRepository.count());
        stats.put("visibleItems", itemRepository.countByModerationStatus(ModerationStatus.VISIBLE));
        stats.put("blindedItems", itemRepository.countByModerationStatus(ModerationStatus.BLINDED));
        stats.put("deletedItems", itemRepository.countByModerationStatus(ModerationStatus.DELETED));
        return stats;
    }

    // 6. 신고된 아이템 조회 (관리 필요한 아이템)
    @Transactional(readOnly = true)
    public List<Item> getReportedItems() {
        List<ModerationStatus> problematicStatuses = Arrays.asList(
                ModerationStatus.BLINDED,
                ModerationStatus.DELETED
        );
        return itemRepository.findByModerationStatusInOrderByCreatedAtDesc(problematicStatuses);
    }

    // 7. 최근 등록된 아이템 (관리자용)
    @Transactional(readOnly = true)
    public List<Item> getRecentItemsForAdmin(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return itemRepository.findByCreatedAtAfterOrderByCreatedAtDesc(cutoffDate); // Repository에 추가 필요
    }
}