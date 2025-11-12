package com.carrot.repository;

import com.carrot.entity.AdminLog;
import com.carrot.constant.ModerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AdminLog 엔티티에 대한 JPA Repository
 * - 기본적인 CRUD 기능 제공
 * - 관리자 로그 조회를 위한 커스텀 쿼리 메소드들
 */
@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    /**
     * 특정 아이템의 모든 관리 이력 조회 (최신순)
     *
     * @param itemId 아이템 ID
     * @return 해당 아이템의 관리 로그 목록
     */
    List<AdminLog> findByItemIdOrderByModeratedAtDesc(Long itemId);

    /**
     * 특정 아이템의 현재(최신) 상태 조회
     *
     * @param itemId 아이템 ID
     * @return 최신 관리 로그
     */
    Optional<AdminLog> findTopByItemIdOrderByModeratedAtDesc(Long itemId);

    /**
     * 특정 관리자가 처리한 모든 로그 조회
     *
     * @param moderatorEmail 관리자 이메일
     * @return 해당 관리자가 처리한 로그 목록
     */
    List<AdminLog> findByModeratorEmailOrderByModeratedAtDesc(String moderatorEmail);

    /**
     * 특정 상태의 모든 로그 조회
     *
     * @param status 검색할 상태 (VISIBLE, BLINDED, DELETED)
     * @return 해당 상태의 로그 목록
     */
    List<AdminLog> findByModerationStatusOrderByModeratedAtDesc(ModerationStatus status);

    /**
     * 기간별 관리 로그 조회
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 해당 기간의 관리 로그 목록
     */
    List<AdminLog> findByModeratedAtBetweenOrderByModeratedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 특정 키워드가 포함된 사유를 가진 로그 검색
     *
     * @param keyword 검색 키워드
     * @return 사유에 키워드가 포함된 로그 목록
     */
    List<AdminLog> findByModerationReasonContainingIgnoreCaseOrderByModeratedAtDesc(String keyword);

    /**
     * 관리자별 처리 통계 조회 (JPQL 사용)
     *
     * @return 관리자별 처리 건수 통계
     */
    @Query("SELECT a.moderatorEmail, COUNT(a) as count " +
            "FROM AdminLog a " +
            "GROUP BY a.moderatorEmail " +
            "ORDER BY count DESC")
    List<Object[]> findModerationStatsByModerator();

    /**
     * 상태별 통계 조회 (JPQL 사용)
     *
     * @return 상태별 건수 통계
     */
    @Query("SELECT a.moderationStatus, COUNT(a) as count " +
            "FROM AdminLog a " +
            "GROUP BY a.moderationStatus " +
            "ORDER BY count DESC")
    List<Object[]> findModerationStatsByStatus();

    /**
     * 최근 N일간의 관리 로그 조회
     *
     * @param days 최근 며칠
     * @return 최근 N일간의 로그 목록
     */
    @Query("SELECT a FROM AdminLog a " +
            "WHERE a.moderatedAt >= :startDate " +
            "ORDER BY a.moderatedAt DESC")
    List<AdminLog> findRecentLogs(@Param("startDate") LocalDateTime startDate);

    /**
     * 특정 아이템이 블라인드 상태인지 확인
     *
     * @param itemId 아이템 ID
     * @return 블라인드 상태면 true
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM AdminLog a " +
            "WHERE a.itemId = :itemId " +
            "AND a.moderationStatus IN ('BLINDED', 'DELETED') " +
            "AND a.moderatedAt = (" +
            "    SELECT MAX(a2.moderatedAt) " +
            "    FROM AdminLog a2 " +
            "    WHERE a2.itemId = :itemId" +
            ")")
    boolean isItemBlocked(@Param("itemId") Long itemId);

    /**
     * 자동 필터링으로 처리된 로그들 조회
     *
     * @return 자동 필터링 로그 목록
     */
    @Query("SELECT a FROM AdminLog a " +
            "WHERE a.moderationReason LIKE '%부적절한 단어 감지%' " +
            "ORDER BY a.moderatedAt DESC")
    List<AdminLog> findAutoModerationLogs();

    long countByModeratedAtAfter(LocalDateTime dateTime);
}