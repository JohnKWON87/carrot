package com.carrot.repository;

import com.carrot.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;   // ✅ 수정
import org.springframework.data.jpa.repository.Query;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    /** 모든 데이터 삭제 후 ID 시퀀스를 1부터 다시 시작 */
    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE agreements ALTER COLUMN id RESTART WITH 1", nativeQuery = true)
    void resetIdSequence();
}
