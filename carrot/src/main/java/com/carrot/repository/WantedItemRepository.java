package com.carrot.repository;

import com.carrot.entity.WantedItem;
import com.carrot.entity.User;
import com.carrot.constant.WantedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WantedItemRepository extends JpaRepository<WantedItem, Long> {

    /**
     * 활성 구매희망상품들만 조회 (최신순)
     */
    List<WantedItem> findByWantedStatusOrderByCreatedAtDesc(WantedStatus wantedStatus);

    /**
     * 활성 구매희망상품들을 페이징으로 조회
     */
    Page<WantedItem> findByWantedStatusOrderByCreatedAtDesc(WantedStatus wantedStatus, Pageable pageable);

    /**
     * 카테고리별 구매희망상품 조회
     */
    List<WantedItem> findByCategoryAndWantedStatusOrderByCreatedAtDesc(String category, WantedStatus wantedStatus);

    /**
     * 특정 구매자의 구매희망상품 목록 조회
     */
    List<WantedItem> findByBuyerOrderByCreatedAtDesc(User buyer);

    /**
     * 특정 구매자의 특정 상태 구매희망상품 조회
     */
    List<WantedItem> findByBuyerAndWantedStatusOrderByCreatedAtDesc(User buyer, WantedStatus wantedStatus);

    /**
     * 제목 또는 설명으로 검색
     */
    @Query("SELECT w FROM WantedItem w WHERE " +
            "(LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND w.wantedStatus = :wantedStatus ORDER BY w.createdAt DESC")
    List<WantedItem> findByTitleOrDescriptionContaining(@Param("keyword") String keyword,
                                                        @Param("wantedStatus") WantedStatus wantedStatus);

    /**
     * 카테고리와 키워드로 검색
     */
    @Query("SELECT w FROM WantedItem w WHERE " +
            "(LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND w.category = :category " +
            "AND w.wantedStatus = :wantedStatus ORDER BY w.createdAt DESC")
    List<WantedItem> findByKeywordAndCategory(@Param("keyword") String keyword,
                                              @Param("category") String category,
                                              @Param("wantedStatus") WantedStatus wantedStatus);

    /**
     * 전체 구매희망상품 수
     */
    long countByWantedStatus(WantedStatus wantedStatus);

    /**
     * 카테고리별 구매희망상품 수
     */
    long countByCategoryAndWantedStatus(String category, WantedStatus wantedStatus);

    /**
     * 최근 N개 구매희망상품 조회
     */
    @Query("SELECT w FROM WantedItem w WHERE w.wantedStatus = :wantedStatus ORDER BY w.createdAt DESC")
    List<WantedItem> findRecentWantedItems(@Param("wantedStatus") WantedStatus wantedStatus, Pageable pageable);
}