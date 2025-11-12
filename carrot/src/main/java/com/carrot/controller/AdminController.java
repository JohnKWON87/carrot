package com.carrot.controller;

import com.carrot.entity.AdminLog;
import com.carrot.entity.Item;
import com.carrot.entity.User;
import com.carrot.entity.AdminMenu;
import com.carrot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @Autowired
    private AdminMenuService adminMenuService;

    // 관리자 권한 체크
    private boolean checkAdminAccess(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return false;
        }
        if (!user.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "관리자 권한이 필요합니다.");
            return false;
        }
        return true;
    }


    // 관리자 메인
    @GetMapping({"", "/"})
    public String adminMain(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/";
        }
        User admin = (User) session.getAttribute("user");
        model.addAttribute("user", admin);

        // 활성화된 게시판 메뉴 추가
        try {
            List<AdminMenu> customMenus = adminMenuService.getActiveMenus();
            model.addAttribute("customMenus", customMenus);
        } catch (Exception e) {
            model.addAttribute("customMenus", new ArrayList<>());
        }

        // 실시간 통계 추가
        try {
            long totalUsers = userService.getAllUsers().size();
            long totalItems = itemService.getAllItemsForAdmin().size();

            // 오늘 등록된 상품 (최근 24시간)
            java.time.LocalDateTime yesterday = java.time.LocalDateTime.now().minusDays(1);
            long todayItems = itemService.getAllItemsForAdmin().stream()
                    .filter(item -> item.getCreatedAt().isAfter(yesterday))
                    .count();

            // 판매중인 상품
            long activeItems = itemService.getAllItemsForAdmin().stream()
                    .filter(item -> item.getSellStatus() == com.carrot.constant.ItemSellStatus.SELL)
                    .count();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("todayItems", todayItems);
            model.addAttribute("activeItems", activeItems);

        } catch (Exception e) {
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("todayItems", 0);
            model.addAttribute("activeItems", 0);
        }

        return "main-admin";
    }

    // 사용자 관리
    @GetMapping("/users")
    public String usersPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/";
        }
        User admin = (User) session.getAttribute("user");
        model.addAttribute("user", admin);
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
        } catch (Exception e) {
            model.addAttribute("error", "사용자 목록을 불러올 수 없습니다: " + e.getMessage());
        }
        return "admin/user-management";
    }

    // 상품 관리
    @GetMapping("/products")
    public String productsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/";
        }
        User admin = (User) session.getAttribute("user");
        model.addAttribute("user", admin);
        try {
            List<Item> items = itemService.getAllItemsForAdmin();
            model.addAttribute("items", items);
        } catch (Exception e) {
            model.addAttribute("error", "상품 목록을 불러올 수 없습니다: " + e.getMessage());
        }
        return "admin/item-management";
    }

    // 관리 로그
    @GetMapping("/logs")
    public String adminLogsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/";
        }
        User admin = (User) session.getAttribute("user");
        model.addAttribute("user", admin);
        try {
            List<AdminLog> recentLogs = adminLogService.getRecentLogs(30);
            model.addAttribute("logs", recentLogs);
        } catch (Exception e) {
            model.addAttribute("error", "관리 로그를 불러올 수 없습니다: " + e.getMessage());
        }
        return "admin/admin-logs";
    }

    // 시스템 설정
    @GetMapping("/settings")
    public String settingsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/";
        }
        User admin = (User) session.getAttribute("user");
        model.addAttribute("user", admin);
        try {
            model.addAttribute("adminMenus", adminMenuService.getAllMenus());
            model.addAttribute("systemStats", systemSettingsService.getSystemStats());
            model.addAttribute("categoryStats", systemSettingsService.getCategoryStats());
            model.addAttribute("recentActivities", systemSettingsService.getRecentActivities());
            model.addAttribute("weeklyStats", systemSettingsService.getWeeklyStats());
            model.addAttribute("dbInfo", systemSettingsService.getDatabaseInfo());
        } catch (Exception e) {
            model.addAttribute("error", "시스템 정보를 불러올 수 없습니다: " + e.getMessage());
        }
        return "admin/system-settings";
    }

    // ===== API 엔드포인트 =====

    // 아이템 관리
    @PostMapping("/item/{itemId}/moderate")
    @ResponseBody
    public ResponseEntity<?> moderateItem(@PathVariable Long itemId, @RequestParam String action,
                                          @RequestParam String reason, HttpSession session) {
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }
            AdminLog result = null;
            switch (action) {
                case "blind": result = adminLogService.blindItem(itemId, reason, admin.getEmail()); break;
                case "delete": result = adminLogService.deleteItem(itemId, reason, admin.getEmail()); break;
                case "restore": result = adminLogService.restoreItem(itemId, admin.getEmail()); break;
                default: return ResponseEntity.badRequest().body("올바르지 않은 액션입니다.");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 사용자 상태 토글
    @PostMapping("/user/{userId}/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, HttpSession session) {
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }
            User user = userService.toggleUserStatus(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 사용자 추방
    @PostMapping("/user/{userId}/ban")
    @ResponseBody
    public ResponseEntity<?> banUser(@PathVariable Long userId, @RequestParam String reason, HttpSession session) {
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }
            User user = userService.banUser(userId, reason, admin.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 통계 API
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<?> getStats(HttpSession session) {
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }
            Map<String, Object> allStats = new HashMap<>();
            allStats.put("systemStats", systemSettingsService.getSystemStats());
            allStats.put("categoryStats", systemSettingsService.getCategoryStats());
            allStats.put("weeklyStats", systemSettingsService.getWeeklyStats());
            allStats.put("dbInfo", systemSettingsService.getDatabaseInfo());
            return ResponseEntity.ok(allStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ===== 메뉴 관리 API =====

    @GetMapping("/menu/{id}")
    @ResponseBody
    public AdminMenu getMenu(@PathVariable Long id) {
        return adminMenuService.getMenuById(id);
    }

    @PostMapping("/menu/add")
    @ResponseBody
    public Map<String, Object> addMenu(@RequestBody AdminMenu menu, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                response.put("success", false);
                response.put("error", "관리자 권한이 필요합니다.");
                return response;
            }
            AdminMenu savedMenu = adminMenuService.saveMenu(menu);
            response.put("success", true);
            response.put("menu", savedMenu);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/menu/{id}/update")
    @ResponseBody
    public Map<String, Object> updateMenu(@PathVariable Long id, @RequestBody AdminMenu menu, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                response.put("success", false);
                response.put("error", "관리자 권한이 필요합니다.");
                return response;
            }
            AdminMenu existingMenu = adminMenuService.getMenuById(id);
            existingMenu.setMenuName(menu.getMenuName());
            existingMenu.setMenuUrl(menu.getMenuUrl());
            existingMenu.setMenuIcon(menu.getMenuIcon());
            existingMenu.setDisplayOrder(menu.getDisplayOrder());
            existingMenu.setDescription(menu.getDescription());
            existingMenu.setEnabled(menu.getEnabled());
            existingMenu.setBoardType(menu.getBoardType());
            AdminMenu updatedMenu = adminMenuService.saveMenu(existingMenu);
            response.put("success", true);
            response.put("menu", updatedMenu);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/menu/{id}/delete")
    @ResponseBody
    public Map<String, Object> deleteMenu(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                response.put("success", false);
                response.put("error", "관리자 권한이 필요합니다.");
                return response;
            }
            adminMenuService.deleteMenu(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/menu/{id}/toggle")
    @ResponseBody
    public Map<String, Object> toggleMenu(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !admin.isAdmin()) {
                response.put("success", false);
                response.put("error", "관리자 권한이 필요합니다.");
                return response;
            }
            adminMenuService.toggleMenu(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // 대시보드 (실시간 현황)
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!checkAdminAccess(session, redirectAttributes)) {
            return "redirect:/";
        }

        User admin = (User) session.getAttribute("user");
        model.addAttribute("user", admin);

        try {
            // 기본 통계
            long totalUsers = userService.getAllUsers().size();
            long totalItems = itemService.getAllItemsForAdmin().size();

            // 오늘 등록된 상품 (최근 24시간)
            java.time.LocalDateTime yesterday = java.time.LocalDateTime.now().minusDays(1);
            long todayItems = itemService.getAllItemsForAdmin().stream()
                    .filter(item -> item.getCreatedAt().isAfter(yesterday))
                    .count();

            // 판매중인 상품
            long activeItems = itemService.getAllItemsForAdmin().stream()
                    .filter(item -> item.getSellStatus() == com.carrot.constant.ItemSellStatus.SELL)
                    .count();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalItems", totalItems);
            model.addAttribute("todayItems", todayItems);
            model.addAttribute("activeItems", activeItems);

        } catch (Exception e) {
            model.addAttribute("error", "통계 정보를 불러올 수 없습니다: " + e.getMessage());
        }

        return "admin/dashboard";
    }
}