// src/main/java/com/carrot/service/AdminMenuService.java
package com.carrot.service;

import com.carrot.entity.AdminMenu;
import com.carrot.repository.AdminMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminMenuService {

    private final AdminMenuRepository adminMenuRepository;

    // 활성화된 메뉴 조회
    public List<AdminMenu> getActiveMenus() {
        return adminMenuRepository.findByEnabledTrueOrderByDisplayOrderAsc();
    }

    // 모든 메뉴 조회
    public List<AdminMenu> getAllMenus() {
        return adminMenuRepository.findAllByOrderByDisplayOrderAsc();
    }

    // 메뉴 저장
    public AdminMenu saveMenu(AdminMenu menu) {
        return adminMenuRepository.save(menu);
    }

    // 메뉴 삭제
    public void deleteMenu(Long id) {
        adminMenuRepository.deleteById(id);
    }

    // 메뉴 조회
    public AdminMenu getMenuById(Long id) {
        return adminMenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));
    }

    // 메뉴 활성화/비활성화 토글
    public void toggleMenu(Long id) {
        AdminMenu menu = getMenuById(id);
        menu.setEnabled(!menu.getEnabled());
        adminMenuRepository.save(menu);
    }
}