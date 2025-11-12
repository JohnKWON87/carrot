package com.carrot.repository;

import com.carrot.entity.Wishlist;
import com.carrot.entity.User;
import com.carrot.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * 특정 사용자와 상품으로 찜 정보 조회
     */
    Optional<Wishlist> findByUserAndItem(User user, Item item);

    /**
     * 사용자와 상품으로 찜 여부 확인
     */
    boolean existsByUserAndItem(User user, Item item);

    /**
     * 특정 사용자의 모든 찜 목록 조회 (최신순)
     */
    List<Wishlist> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 사용자의 찜 목록 조회 (페이징)
     */
    Page<Wishlist> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 특정 상품을 찜한 모든 사용자 목록 조회
     */
    List<Wishlist> findByItemOrderByCreatedAtDesc(Item item);

    /**
     * 특정 상품의 찜 개수 조회
     */
    long countByItem(Item item);

    /**
     * 특정 사용자의 찜 개수 조회
     */
    long countByUser(User user);

    /**
     * 사용자의 찜 목록에서 아직 판매 중인 상품들만 조회
     * ⭐ JOIN FETCH 추가 - LAZY 로딩 문제 해결!
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.item i " +
            "JOIN FETCH i.seller " +
            "WHERE w.user = :user " +
            "AND i.sellStatus = com.carrot.constant.ItemSellStatus.SELL " +
            "AND i.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "ORDER BY w.createdAt DESC")
    List<Wishlist> findAvailableWishlistByUser(@Param("user") User user);

    /**
     * 사용자의 찜 목록에서 카테고리별로 조회
     * ⭐ JOIN FETCH 추가
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.item i " +
            "JOIN FETCH i.seller " +
            "WHERE w.user = :user " +
            "AND i.category = :category " +
            "AND i.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserAndItemCategory(@Param("user") User user,
                                             @Param("category") String category);

    /**
     * 특정 사용자의 찜 목록을 페이징으로 조회하되,
     * 표시 가능한(VISIBLE) 상품만 포함
     * ⭐ JOIN FETCH 추가
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.item i " +
            "JOIN FETCH i.seller " +
            "WHERE w.user = :user " +
            "AND i.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "ORDER BY w.createdAt DESC")
    Page<Wishlist> findVisibleWishlistByUser(@Param("user") User user, Pageable pageable);

    /**
     * 상품별 찜 개수 통계 (인기 상품 분석용)
     */
    @Query("SELECT w.item.id, COUNT(w) as wishCount FROM Wishlist w " +
            "WHERE w.item.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "GROUP BY w.item.id " +
            "ORDER BY wishCount DESC")
    List<Object[]> findItemWishStatistics();

    /**
     * 최근 N일간 찜한 상품들 조회
     * ⭐ JOIN FETCH 추가
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.item i " +
            "JOIN FETCH i.seller " +
            "WHERE w.user = :user " +
            "AND w.createdAt >= CURRENT_TIMESTAMP - :days DAY " +
            "AND i.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "ORDER BY w.createdAt DESC")
    List<Wishlist> findRecentWishlistByUser(@Param("user") User user, @Param("days") int days);

    /**
     * 사용자별 찜한 상품의 평균 가격 계산
     */
    @Query("SELECT AVG(w.item.price) FROM Wishlist w WHERE w.user = :user")
    Double findAverageWishlistPriceByUser(@Param("user") User user);

    /**
     * 특정 가격 범위 내의 찜한 상품들 조회
     * ⭐ JOIN FETCH 추가
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.item i " +
            "JOIN FETCH i.seller " +
            "WHERE w.user = :user " +
            "AND i.price BETWEEN :minPrice AND :maxPrice " +
            "AND i.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "ORDER BY w.createdAt DESC")
    List<Wishlist> findWishlistByUserAndPriceRange(@Param("user") User user,
                                                   @Param("minPrice") Integer minPrice,
                                                   @Param("maxPrice") Integer maxPrice);

    /**
     * 사용자가 찜한 상품 중 특정 키워드가 포함된 상품들 검색
     * ⭐ JOIN FETCH 추가
     */
    @Query("SELECT w FROM Wishlist w " +
            "JOIN FETCH w.item i " +
            "JOIN FETCH i.seller " +
            "WHERE w.user = :user " +
            "AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND i.moderationStatus = com.carrot.constant.ModerationStatus.VISIBLE " +
            "ORDER BY w.createdAt DESC")
    List<Wishlist> searchWishlistByUserAndKeyword(@Param("user") User user,
                                                  @Param("keyword") String keyword);
}