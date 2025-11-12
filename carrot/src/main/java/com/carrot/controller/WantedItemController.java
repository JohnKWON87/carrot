package com.carrot.controller;

import com.carrot.entity.WantedItem;
import com.carrot.entity.User;
import com.carrot.service.WantedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/wanted")
public class WantedItemController {

    @Autowired
    private WantedItemService wantedItemService;

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
     * 구매희망상품 등록 폼 페이지 (GET /wanted/register)
     */
    @GetMapping("/register")
    public String wantedItemRegisterForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        return "wanted-form";
    }

    /**
     * 구매희망상품 등록 처리 (POST /wanted/register)
     */
    @PostMapping("/register")
    public String registerWantedItem(@RequestParam String title,
                                     @RequestParam String description,
                                     @RequestParam Integer maxPrice,
                                     @RequestParam String category,
                                     @RequestParam String location,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            WantedItem wantedItem = new WantedItem();
            wantedItem.setTitle(title);
            wantedItem.setDescription(description);
            wantedItem.setMaxPrice(maxPrice);
            wantedItem.setCategory(category);
            wantedItem.setLocation(location);
            wantedItem.setBuyer(user);

            WantedItem savedWantedItem = wantedItemService.registerWantedItem(wantedItem);

            redirectAttributes.addFlashAttribute("success",
                    "구매희망상품이 성공적으로 등록되었습니다! 등록번호: " + savedWantedItem.getId());

            return "redirect:/wanted/" + savedWantedItem.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/wanted/register";
        }
    }

    /**
     * 구매희망상품 목록 페이지 (GET /wanted/list)
     */
    @GetMapping("/list")
    public String wantedItemList(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            var wantedItemsPage = wantedItemService.getWantedItemsWithPaging(page, size);

            model.addAttribute("wantedItems", wantedItemsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", wantedItemsPage.getTotalPages());
            model.addAttribute("totalItems", wantedItemsPage.getTotalElements());
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "wanted-list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 구매희망상품 상세 페이지 (GET /wanted/{id})
     * ✅ 이 메서드가 올바른 위치에 있어야 합니다
     */
    @GetMapping("/{id}")
    public String wantedItemDetail(@PathVariable Long id,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            WantedItem wantedItem = wantedItemService.getWantedItemDetail(id);

            // 구매자 본인 여부 확인
            boolean isOwner = wantedItem.getBuyer().getId().equals(user.getId());

            model.addAttribute("wantedItem", wantedItem);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "wanted-detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품을 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/wanted/list";
        }
    }

    /**
     * 내 구매희망상품 목록 페이지 (GET /wanted/my)
     */
    @GetMapping("/my")
    public String myWantedItems(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            List<WantedItem> myWantedItems = wantedItemService.getWantedItemsByBuyer(user);

            model.addAttribute("wantedItems", myWantedItems);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "my-wanted-items";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "내 구매희망상품을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 구매희망상품 수정 폼 페이지 (GET /wanted/edit/{id})
     */
    @GetMapping("/edit/{id}")
    public String editWantedItemForm(@PathVariable Long id,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            WantedItem wantedItem = wantedItemService.getWantedItemDetail(id);

            if (!wantedItem.getBuyer().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "구매희망상품을 수정할 권한이 없습니다.");
                return "redirect:/wanted/" + id;
            }

            model.addAttribute("wantedItem", wantedItem);
            model.addAttribute("user", user);
            model.addAttribute("username", user.getUsername());

            return "wanted-edit";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품을 찾을 수 없습니다: " + e.getMessage());
            return "redirect:/wanted/my";
        }
    }

    /**
     * 구매희망상품 수정 처리 (POST /wanted/edit/{id})
     */
    @PostMapping("/edit/{id}")
    public String editWantedItem(@PathVariable Long id,
                                 @RequestParam String title,
                                 @RequestParam String description,
                                 @RequestParam Integer maxPrice,
                                 @RequestParam String category,
                                 @RequestParam String location,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            WantedItem updatedWantedItem = new WantedItem();
            updatedWantedItem.setTitle(title);
            updatedWantedItem.setDescription(description);
            updatedWantedItem.setMaxPrice(maxPrice);
            updatedWantedItem.setCategory(category);
            updatedWantedItem.setLocation(location);

            WantedItem savedWantedItem = wantedItemService.updateWantedItem(id, updatedWantedItem, user);

            redirectAttributes.addFlashAttribute("success", "구매희망상품이 성공적으로 수정되었습니다.");
            return "redirect:/wanted/" + savedWantedItem.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/wanted/edit/" + id;
        }
    }

    /**
     * 구매희망상품 삭제 처리 (POST /wanted/delete/{id})
     */
    @PostMapping("/delete/{id}")
    public String deleteWantedItem(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            wantedItemService.deleteWantedItem(id, user);

            redirectAttributes.addFlashAttribute("success", "구매희망상품이 성공적으로 삭제되었습니다.");
            return "redirect:/wanted/my";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/wanted/" + id;
        }
    }

    /**
     * 구매희망상품 상태 변경 처리 (POST /wanted/status/{id})
     */
    @PostMapping("/status/{id}")
    public String changeWantedItemStatus(@PathVariable Long id,
                                         @RequestParam String status,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {

        User user = checkLogin(session, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }

        try {
            com.carrot.constant.WantedStatus newStatus;
            switch (status.toUpperCase()) {
                case "ACTIVE":
                    newStatus = com.carrot.constant.WantedStatus.ACTIVE;
                    break;
                case "MATCHED":
                    newStatus = com.carrot.constant.WantedStatus.MATCHED;
                    break;
                case "CANCELLED":
                    newStatus = com.carrot.constant.WantedStatus.CANCELLED;
                    break;
                default:
                    throw new IllegalArgumentException("잘못된 상태값입니다.");
            }

            wantedItemService.changeWantedItemStatus(id, newStatus, user);

            String statusMessage = newStatus == com.carrot.constant.WantedStatus.ACTIVE ? "구매희망 중" :
                    newStatus == com.carrot.constant.WantedStatus.MATCHED ? "매칭됨" : "취소됨";

            redirectAttributes.addFlashAttribute("success",
                    "구매희망상품 상태가 '" + statusMessage + "'으로 변경되었습니다.");

            return "redirect:/wanted/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "구매희망상품 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/wanted/" + id;
        }
    } // ✅ changeWantedItemStatus 메서드가 여기서 끝납니다



} // ✅ 클래스가 여기서 끝납니다