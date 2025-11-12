package com.carrot.controller;

import com.carrot.entity.Item;
import com.carrot.entity.User;
import com.carrot.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import com.carrot.entity.Agreement;// 박정대 파일

import java.util.List;

@Controller
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

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
     * 상품 등록 폼 페이지 (GET /item/register)
     */
    @GetMapping("/register")
    public String itemRegisterForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        return "item-form"; // 상품 등록 폼 페이지
    }

    /**
     * 상품 등록 처리 (POST /item/register)
     */
    @PostMapping("/register")
    public String registerItem(@RequestParam String title,
                               @RequestParam String description,
                               @RequestParam Integer price,
                               @RequestParam String category,
                               @RequestParam String location,
                               @RequestParam(required = false) String imageUrl,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            // Item 객체 생성
            Item item = new Item();
            item.setTitle(title);
            item.setDescription(description);
            item.setPrice(price);
            item.setCategory(category);
            item.setLocation(location);
            item.setSeller(user);

            // 상품 등록 서비스 호출
            Item savedItem = itemService.registerItem(item);

            redirectAttributes.addFlashAttribute("success",
                    "상품이 성공적으로 등록되었습니다! 상품 번호: " + savedItem.getId());

            return "redirect:/item/" + savedItem.getId(); // 등록한 상품 상세 페이지로 이동

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/item/register";
        }
    }

    /**
     * 상품 목록 페이지 (GET /item/list)
     */
    @GetMapping("/list")
    public String itemList(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            var itemsPage = itemService.getItemsWithPaging(page, size);

            model.addAttribute("items", itemsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", itemsPage.getTotalPages());
            model.addAttribute("totalItems", itemsPage.getTotalElements());
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "item-list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 상품 상세 페이지 (GET /item/{id})
     */
    @GetMapping("/{id}")
    public String itemDetail(@PathVariable Long id,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            Item item = itemService.getItemDetail(id);

            // 판매자 본인 여부 확인
            boolean isOwner = item.getSeller().getId().equals(user.getId());

            model.addAttribute("item", item);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "item-detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품을 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 내 상품 목록 페이지 (GET /item/my)
     * 5단계: 사용자별 등록 상품 목록
     */
    @GetMapping("/my")
    public String myItems(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            // 🔍 더 자세한 디버깅
            System.out.println("===== MY ITEMS DEBUG =====");
            System.out.println("User ID: " + user.getId());
            System.out.println("User: " + user.getUsername());

            List<Item> myItems = itemService.getItemsBySeller(user);

            System.out.println("Query result: " + myItems);
            System.out.println("Items count: " + (myItems != null ? myItems.size() : "null"));

            if (myItems != null) {
                // 상태별 개수 계산
                long sellCount = myItems.stream()
                        .filter(item -> item.getSellStatus() == com.carrot.constant.ItemSellStatus.SELL)
                        .count();

                long reservedCount = myItems.stream()
                        .filter(item -> item.getSellStatus() == com.carrot.constant.ItemSellStatus.RESERVED)
                        .count();

                long soldCount = myItems.stream()
                        .filter(item -> item.getSellStatus() == com.carrot.constant.ItemSellStatus.SOLD_OUT)
                        .count();

                // 템플릿에 전달
                model.addAttribute("sellCount", sellCount);
                model.addAttribute("reservedCount", reservedCount);
                model.addAttribute("soldCount", soldCount);

                System.out.println("판매중: " + sellCount + ", 예약중: " + reservedCount + ", 판매완료: " + soldCount);

                for (int i = 0; i < myItems.size(); i++) {
                    Item item = myItems.get(i);
                    System.out.println("Item " + i + ": " + item.getTitle() + ", Price: " + item.getPrice() + ", Seller ID: " + item.getSeller().getId());
                }
            }
            System.out.println("==========================");

            model.addAttribute("items", myItems);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "my-items";

        } catch (Exception e) {
            System.err.println("ERROR in myItems: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "내 상품을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 상품 수정 폼 페이지 (GET /item/edit/{id})
     */
    @GetMapping("/edit/{id}")
    public String editItemForm(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            Item item = itemService.getItemDetail(id);

            // 수정 권한 확인 (본인 또는 관리자만)
            if (!item.getSeller().getId().equals(user.getId()) && !user.isAdmin()) {
                redirectAttributes.addFlashAttribute("error", "상품을 수정할 권한이 없습니다.");
                return "redirect:/item/" + id;
            }

            model.addAttribute("item", item);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "item-edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품을 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/item/my";
        }
    }

    /**
     * 상품 수정 처리 (POST /item/edit/{id})
     */
    @PostMapping("/edit/{id}")
    public String editItem(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam String description,
                           @RequestParam Integer price,
                           @RequestParam String category,
                           @RequestParam String location,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            // 수정할 상품 정보 생성
            Item updatedItem = new Item();
            updatedItem.setTitle(title);
            updatedItem.setDescription(description);
            updatedItem.setPrice(price);
            updatedItem.setCategory(category);
            updatedItem.setLocation(location);

            // 상품 수정 서비스 호출
            Item savedItem = itemService.updateItem(id, updatedItem, user);

            redirectAttributes.addFlashAttribute("success", "상품이 성공적으로 수정되었습니다.");
            return "redirect:/item/" + savedItem.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/item/edit/" + id;
        }
    }

    /**
     * 상품 삭제 처리 (POST /item/delete/{id})
     */
    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            itemService.deleteItem(id, user);

            redirectAttributes.addFlashAttribute("success", "상품이 성공적으로 삭제되었습니다.");
            return "redirect:/item/my";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/item/" + id;
        }
    }

    /**
     * 상품 상태 변경 처리 (POST /item/status/{id})
     * 판매중 -> 예약중 -> 판매완료
     */
    @PostMapping("/status/{id}")
    public String changeItemStatus(@PathVariable Long id,
                                   @RequestParam String status,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            // String을 ItemSellStatus로 변환
            com.carrot.constant.ItemSellStatus newStatus;
            switch (status.toUpperCase()) {
                case "SELL":
                    newStatus = com.carrot.constant.ItemSellStatus.SELL;
                    break;
                case "RESERVED":
                    newStatus = com.carrot.constant.ItemSellStatus.RESERVED;
                    break;
                case "SOLD_OUT":
                    newStatus = com.carrot.constant.ItemSellStatus.SOLD_OUT;
                    break;
                default:
                    throw new IllegalArgumentException("잘못된 상태값입니다.");
            }

            itemService.changeItemStatus(id, newStatus, user);

            String statusMessage = newStatus == com.carrot.constant.ItemSellStatus.SELL ? "판매중" :
                    newStatus == com.carrot.constant.ItemSellStatus.RESERVED ? "예약중" : "판매완료";

            redirectAttributes.addFlashAttribute("success",
                    "상품 상태가 '" + statusMessage + "'으로 변경되었습니다.");

            return "redirect:/item/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "상품 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/item/" + id;
        }
    }
}