package com.carrot.repository;

import com.carrot.entity.Item;
import com.carrot.entity.User;
import com.carrot.constant.ItemSellStatus;
import com.carrot.constant.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Item 엔티티에 대한 JPA Repository
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // ===== 기본 조회 메서드 =====

    /**
     * 판매 가능한 상품들만 조회 (관리자에 의해 숨겨지지 않은 상품)
     */
    List<Item> findByModerationStatusOrderByCreatedAtDesc(ModerationStatus moderationStatus);

    /**
     * 판매 가능한 상품들을 페이징으로 조회
     */
    Page<Item> findByModerationStatusOrderByCreatedAtDesc(ModerationStatus moderationStatus, Pageable pageable);

    // ===== 카테고리별 조회 =====

    /**
     * 카테고리별 상품 조회
     */
    List<Item> findByCategoryAndModerationStatusOrderByCreatedAtDesc(String category, ModerationStatus moderationStatus);

    /**
     * 카테고리별 상품 조회 (페이징)
     */
    Page<Item> findByCategoryAndModerationStatusOrderByCreatedAtDesc(String category, ModerationStatus moderationStatus, Pageable pageable);

    // ===== 판매자별 조회 =====

    /**
     * 특정 판매자의 상품 목록 조회
     */
    @Query("SELECT i FROM Item i " +
            "JOIN FETCH i.seller " +
            "WHERE i.seller = :seller " +
            "ORDER BY i.createdAt DESC")
    List<Item> findBySellerOrderByCreatedAtDesc(User seller);

    /**
     * 특정 판매자의 상품 목록 조회 (페이징)
     */
    Page<Item> findBySellerOrderByCreatedAtDesc(User seller, Pageable pageable);

    /**
     * 특정 판매자의 특정 상태 상품 조회
     */
    List<Item> findBySellerAndSellStatusOrderByCreatedAtDesc(User seller, ItemSellStatus sellStatus);

    // ===== 검색 기능 =====

    /**
     * 제목으로 검색 (부분 일치, 대소문자 무시)
     */
    @Query("SELECT i FROM Item i WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND i.moderationStatus = :moderationStatus ORDER BY i.createdAt DESC")
    List<Item> findByTitleContainingIgnoreCase(@Param("keyword") String keyword,
                                               @Param("moderationStatus") ModerationStatus moderationStatus);

    /**
     * 제목 또는 설명으로 검색
     */
    @Query("SELECT i FROM Item i WHERE " +
            "(LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND i.moderationStatus = :moderationStatus ORDER BY i.createdAt DESC")
    List<Item> findByTitleOrDescriptionContaining(@Param("keyword") String keyword,
                                                  @Param("moderationStatus") ModerationStatus moderationStatus);

    /**
     * 카테고리와 키워드로 검색
     */
    @Query("SELECT i FROM Item i WHERE " +
            "(LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND i.category = :category " +
            "AND i.moderationStatus = :moderationStatus ORDER BY i.createdAt DESC")
    List<Item> findByKeywordAndCategory(@Param("keyword") String keyword,
                                        @Param("category") String category,
                                        @Param("moderationStatus") ModerationStatus moderationStatus);

    // ===== 가격 범위 검색 =====

    /**
     * 가격 범위로 검색
     */
    @Query("SELECT i FROM Item i WHERE i.price BETWEEN :minPrice AND :maxPrice " +
            "AND i.moderationStatus = :moderationStatus ORDER BY i.createdAt DESC")
    List<Item> findByPriceBetween(@Param("minPrice") Integer minPrice,
                                  @Param("maxPrice") Integer maxPrice,
                                  @Param("moderationStatus") ModerationStatus moderationStatus);

    // ===== 지역별 검색 =====

    /**
     * 지역으로 검색
     */
    List<Item> findByLocationContainingIgnoreCaseAndModerationStatusOrderByCreatedAtDesc(
            String location, ModerationStatus moderationStatus);

    // ===== 통계용 메서드 =====

    /**
     * 전체 상품 수 (관리자용)
     */
    long countByModerationStatus(ModerationStatus moderationStatus);

    /**
     * 카테고리별 상품 수
     */
    long countByCategoryAndModerationStatus(String category, ModerationStatus moderationStatus);

    /**
     * 특정 기간 내 등록된 상품 수
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    // ===== 관리자용 메서드 =====

    /**
     * 모든 상품 조회 (관리자용 - 상태 무관)
     */
    List<Item> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태의 상품들 조회 (관리자용)
     */
    List<Item> findBySellStatusOrderByCreatedAtDesc(ItemSellStatus sellStatus);

    /**
     * 검토가 필요한 상품들 (신고된 상품 등)
     */
    List<Item> findByModerationStatusInOrderByCreatedAtDesc(List<ModerationStatus> statuses);

    // ===== 정렬별 조회 =====

    /**
     * 가격 낮은 순으로 정렬
     */
    @Query("SELECT i FROM Item i WHERE i.moderationStatus = :moderationStatus ORDER BY i.price ASC")
    List<Item> findByModerationStatusOrderByPriceAsc(@Param("moderationStatus") ModerationStatus moderationStatus);

    /**
     * 가격 높은 순으로 정렬
     */
    @Query("SELECT i FROM Item i WHERE i.moderationStatus = :moderationStatus ORDER BY i.price DESC")
    List<Item> findByModerationStatusOrderByPriceDesc(@Param("moderationStatus") ModerationStatus moderationStatus);

    /**
     * 조회수 많은 순으로 정렬
     */
    @Query("SELECT i FROM Item i WHERE i.moderationStatus = :moderationStatus ORDER BY i.viewCount DESC")
    List<Item> findByModerationStatusOrderByViewCountDesc(@Param("moderationStatus") ModerationStatus moderationStatus);

    /**
     * 찜 많은 순으로 정렬
     */
    @Query("SELECT i FROM Item i WHERE i.moderationStatus = :moderationStatus ORDER BY i.wishCount DESC")
    List<Item> findByModerationStatusOrderByWishCountDesc(@Param("moderationStatus") ModerationStatus moderationStatus);

    // ===== 최근 상품 조회 =====

    /**
     * 최근 N개 상품 조회
     */
    @Query("SELECT i FROM Item i WHERE i.moderationStatus = :moderationStatus ORDER BY i.createdAt DESC")
    List<Item> findRecentItems(@Param("moderationStatus") ModerationStatus moderationStatus, Pageable pageable);

    /**
     * 특정 카테고리의 최근 상품들
     */
    @Query("SELECT i FROM Item i WHERE i.category = :category AND i.moderationStatus = :moderationStatus ORDER BY i.createdAt DESC")
    List<Item> findRecentItemsByCategory(@Param("category") String category,
                                         @Param("moderationStatus") ModerationStatus moderationStatus,
                                         Pageable pageable);

    Page<Item> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Item> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime cutoffDate);

    List<Item> findTop6ByModerationStatusOrderByCreatedAtDesc(ModerationStatus moderationStatus);
}