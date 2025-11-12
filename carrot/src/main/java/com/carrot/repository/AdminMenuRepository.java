// src/main/java/com/carrot/repository/AdminMenuRepository.java
package com.carrot.repository;

import com.carrot.entity.AdminMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminMenuRepository extends JpaRepository<AdminMenu, Long> {

    // 활성화된 메뉴만 순서대로 조회
    List<AdminMenu> findByEnabledTrueOrderByDisplayOrderAsc();

    // 모든 메뉴를 순서대로 조회
    List<AdminMenu> findAllByOrderByDisplayOrderAsc();
}