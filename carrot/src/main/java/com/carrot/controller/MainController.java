package com.carrot.controller;

import com.carrot.entity.AdminMenu;
import com.carrot.entity.User;
import com.carrot.entity.Item;
import com.carrot.service.AdminMenuService;
import com.carrot.service.UserService;
import com.carrot.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import com.carrot.dto.SearchResultDto;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AdminMenuService adminMenuService;

    // 메인페이지 - 로그인 상태에 따라 다른 페이지 반환
    @GetMapping("/")
    public String mainPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser != null) {
            model.addAttribute("user", loggedInUser);
            model.addAttribute("username", loggedInUser.getUsername());

            // 활성화된 커스텀 메뉴 추가
            try {
                List<AdminMenu> customMenus = adminMenuService.getActiveMenus();
                model.addAttribute("customMenus", customMenus);
            } catch (Exception e) {
                model.addAttribute("customMenus", new ArrayList<>());
            }

            if (loggedInUser.isAdmin()) {
                return "redirect:/admin";
            } else {
                // 최근 상품들 추가 (회원용 메인페이지에 표시)
                try {
                    List<Item> recentItems = itemService.getRecentItems(6);
                    model.addAttribute("recentItems", recentItems);
                } catch (Exception e) {
                    // 에러가 발생해도 메인페이지는 표시
                    model.addAttribute("recentItems", new ArrayList<>());
                }
                return "main-member";
            }
        } else {
            return "main-login";
        }
    }

    // 로그인 처리 (실제 DB 연동)
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        try {
            User user = userService.authenticate(username, password);

            if (user != null) {
                session.setAttribute("user", user);
                session.setAttribute("username", user.getUsername());

                if (user.isAdmin()) {
                    return "redirect:/admin";
                } else {
                    return "redirect:/";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
                return "redirect:/";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "로그인 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * 아이디 중복 체크 API (AJAX 호출용)
     */
    @GetMapping("/api/check-username")
    @ResponseBody
    public Map<String, Object> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (username == null || username.trim().isEmpty()) {
                response.put("available", false);
                response.put("message", "아이디를 입력해주세요.");
                return response;
            }

            if (username.length() < 4 || username.length() > 20) {
                response.put("available", false);
                response.put("message", "아이디는 4~20자 사이여야 합니다.");
                return response;
            }

            if (!Pattern.matches("^[a-zA-Z0-9]+$", username)) {
                response.put("available", false);
                response.put("message", "아이디는 영문, 숫자만 사용 가능합니다.");
                return response;
            }

            boolean exists = userService.isUsernameExists(username);

            if (exists) {
                response.put("available", false);
                response.put("message", "이미 사용 중인 아이디입니다.");
            } else {
                response.put("available", true);
                response.put("message", "사용 가능한 아이디입니다.");
            }

        } catch (Exception e) {
            response.put("available", false);
            response.put("message", "아이디 확인 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 이메일 중복 체크 API (AJAX 호출용)
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("available", false);
                response.put("message", "이메일을 입력해주세요.");
                return response;
            }

            String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!Pattern.matches(emailPattern, email)) {
                response.put("available", false);
                response.put("message", "올바른 이메일 형식을 입력해주세요.");
                return response;
            }

            boolean exists = userService.isEmailExists(email);

            if (exists) {
                response.put("available", false);
                response.put("message", "이미 사용 중인 이메일입니다.");
            } else {
                response.put("available", true);
                response.put("message", "사용 가능한 이메일입니다.");
            }

        } catch (Exception e) {
            response.put("available", false);
            response.put("message", "이메일 확인 중 오류가 발생했습니다.");
        }

        return response;
    }

    // 회원가입 처리 (실제 DB 연동)
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String name,
                           @RequestParam String phone,
                           @RequestParam String email,
                           RedirectAttributes redirectAttributes) {

        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "redirect:/register";
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone(phone);

            userService.registerUser(newUser);

            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // 회원가입 페이지 표시
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // 카테고리별 상품 페이지
    @GetMapping("/category/{category}")
    public String categoryPage(@PathVariable String category,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/";
        }

        try {
            List<Item> items = itemService.getItemsByCategory(category);

            model.addAttribute("category", category);
            model.addAttribute("categoryName", getCategoryName(category));
            model.addAttribute("items", items);
            model.addAttribute("user", loggedInUser);
            model.addAttribute("username", loggedInUser.getUsername());

            return "category";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "카테고리 상품을 불러오는 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }

    // 검색 페이지
    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String keyword,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/";
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            return "redirect:/search/results?keyword=" + keyword;
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("username", loggedInUser.getUsername());
        return "search";
    }

    // 검색 결과 페이지 - 실제 데이터베이스 검색
    @GetMapping("/search/results")
    public String searchResults(@RequestParam String keyword,
                                @RequestParam(defaultValue = "all") String category,
                                @RequestParam(defaultValue = "latest") String sort,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/";
        }

        try {
            // 실제 데이터베이스 검색
            List<Item> searchResults = itemService.searchItemsWithFilters(keyword, category, sort);

            // Item을 SearchResultDto로 변환
            List<SearchResultDto> results = searchResults.stream()
                    .map(this::convertToSearchResultDto)
                    .collect(Collectors.toList());

            model.addAttribute("keyword", keyword);
            model.addAttribute("category", category);
            model.addAttribute("sort", sort);
            model.addAttribute("results", results);
            model.addAttribute("totalResults", results.size());
            model.addAttribute("user", loggedInUser);
            model.addAttribute("username", loggedInUser.getUsername());

            return "search-results";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/search";
        }
    }

    // Item을 SearchResultDto로 변환하는 헬퍼 메서드
    private SearchResultDto convertToSearchResultDto(Item item) {
        return new SearchResultDto(
                item.getId().toString(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getCategory(),
                item.getLocation(),
                item.getTimeAgo(),
                item.getImageUrl() != null ? item.getImageUrl() : "/images/default.jpg",
                false, // 찜 여부 (7단계에서 구현 예정)
                item.getViewCount()
        );
    }

    private String getCategoryName(String category) {
        switch (category) {
            case "clothes": return "의류";
            case "electronics": return "가전제품";
            case "misc": return "기타 및 잡화";
            default: return "기타";
        }
    }
}