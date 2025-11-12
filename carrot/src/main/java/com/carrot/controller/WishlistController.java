package com.carrot.controller;

import com.carrot.entity.User;
import com.carrot.entity.Wishlist;
import com.carrot.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // 로그인 체크 헬퍼 메서드
    private User checkLogin(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return null;
        }
        return user;
    }

    /**
     * 찜하기/취소 API (AJAX)
     */
    @PostMapping("/toggle/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(@PathVariable Long itemId,
                                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean isWishlisted = wishlistService.toggleWishlist(itemId, user);

            response.put("success", true);
            response.put("isWishlisted", isWishlisted);
            response.put("message", isWishlisted ? "찜 목록에 추가되었습니다." : "찜 목록에서 제거되었습니다.");

            // 현재 찜 개수도 함께 반환
            long wishCount = wishlistService.getItemWishlistCount(itemId);
            response.put("wishCount", wishCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 찜 상태 확인 API (AJAX)
     */
    @GetMapping("/status/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistStatus(@PathVariable Long itemId,
                                                                 HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("isWishlisted", false);
                response.put("wishCount", 0);
                return ResponseEntity.ok(response);
            }

            boolean isWishlisted = wishlistService.isWishlisted(itemId, user);
            long wishCount = wishlistService.getItemWishlistCount(itemId);

            response.put("isWishlisted", isWishlisted);
            response.put("wishCount", wishCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("isWishlisted", false);
            response.put("wishCount", 0);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 내 관심상품 목록 페이지 (GET /wishlist)
     */
    @GetMapping({"", "/"})
    public String myWishlist(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             @RequestParam(required = false) String category,
                             @RequestParam(required = false) String keyword,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            // 🔍 디버깅 로그 추가
            System.out.println("===== WISHLIST DEBUG =====");
            System.out.println("User ID: " + user.getId());
            System.out.println("User: " + user.getUsername());
            System.out.println("Category: " + category);
            System.out.println("Keyword: " + keyword);

            List<Wishlist> wishlistItems;

            // 필터 조건에 따른 조회
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 키워드 검색
                wishlistItems = wishlistService.searchUserWishlist(user, keyword.trim());
                System.out.println("Using keyword search");
            } else if (category != null && !category.equals("all")) {
                // 카테고리 필터
                wishlistItems = wishlistService.getUserWishlistByCategory(user, category);
                System.out.println("Using category filter: " + category);
            } else {
                // 전체 조회
                wishlistItems = wishlistService.getUserWishlist(user);
                System.out.println("Using full wishlist query");
            }

            System.out.println("Wishlist query result: " + wishlistItems);
            System.out.println("Wishlist items count: " + (wishlistItems != null ? wishlistItems.size() : "null"));

            if (wishlistItems != null && !wishlistItems.isEmpty()) {
                for (int i = 0; i < Math.min(3, wishlistItems.size()); i++) {
                    Wishlist w = wishlistItems.get(i);
                    System.out.println("Wishlist " + i + ": Item=" + w.getItem().getTitle() + ", User ID=" + w.getUser().getId());
                }
            }

            // 통계 정보
            long totalWishCount = wishlistService.getUserWishlistCount(user);
            Double avgPrice = wishlistService.getUserWishlistAveragePrice(user);

// 판매중인 상품 개수 계산 추가
            long sellCount = 0;
            if (wishlistItems != null) {
                sellCount = wishlistItems.stream()
                        .filter(w -> w.getItem().getSellStatus() == com.carrot.constant.ItemSellStatus.SELL)
                        .count();
            }

            System.out.println("Total wish count: " + totalWishCount);
            System.out.println("Average price: " + avgPrice);
            System.out.println("Sell count: " + sellCount);
            System.out.println("==========================");

            model.addAttribute("wishlistItems", wishlistItems);
            model.addAttribute("totalWishCount", totalWishCount);
            model.addAttribute("avgPrice", avgPrice);
            model.addAttribute("sellCount", sellCount); // 이 줄 추가

            return "my-wishlist";

        } catch (Exception e) {
            System.err.println("ERROR in myWishlist: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "관심상품 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 관심상품에서 제거 (POST /wishlist/remove/{itemId})
     */
    @PostMapping("/remove/{itemId}")
    public String removeFromWishlist(@PathVariable Long itemId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            wishlistService.removeFromWishlist(itemId, user);
            redirectAttributes.addFlashAttribute("success", "관심상품에서 제거되었습니다.");
            return "redirect:/wishlist";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "관심상품 제거 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/wishlist";
        }
    }

    /**
     * 관심상품 일괄 삭제 (POST /wishlist/clear)
     */
    @PostMapping("/clear")
    public String clearWishlist(@RequestParam(required = false) String category,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            List<Wishlist> itemsToRemove;

            if (category != null && !category.equals("all")) {
                itemsToRemove = wishlistService.getUserWishlistByCategory(user, category);
            } else {
                itemsToRemove = wishlistService.getUserWishlist(user);
            }

            for (Wishlist wishlist : itemsToRemove) {
                wishlistService.removeFromWishlist(wishlist.getItem().getId(), user);
            }

            String message = category != null && !category.equals("all")
                    ? category + " 카테고리의 관심상품이 모두 삭제되었습니다."
                    : "모든 관심상품이 삭제되었습니다.";

            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/wishlist";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "관심상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/wishlist";
        }
    }
}