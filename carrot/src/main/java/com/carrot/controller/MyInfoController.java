package com.carrot.controller;

import com.carrot.dto.MyInfoForm;
import com.carrot.entity.User;
import com.carrot.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.net.URI;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MyInfoController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String RETURN_TO_KEY = "RETURN_TO";

    private boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }

    /** 인증 사용자명 가져오기 (세션 fallback 포함) */
    private String resolveUsername(Authentication auth, HttpSession session) {
        if (auth != null && auth.isAuthenticated()) {
            Object p = auth.getPrincipal();
            if (p instanceof UserDetails ud) return ud.getUsername();
            if (p != null && !"anonymousUser".equals(p)) return auth.getName();
        }
        Object u1 = session.getAttribute("LOGIN_USERNAME");
        if (u1 != null) return String.valueOf(u1);
        Object u2 = session.getAttribute("username");
        if (u2 != null) return String.valueOf(u2);
        return null;
    }

    /** open-redirect 방지: 내부 절대경로(/…)만 허용, 이상값 금지 */
    private String sanitizeReturnTo(String url) {
        if (url == null) return null;
        url = url.trim();
        if (url.isEmpty()) return null;
        if (",".equals(url) || "#".equals(url)) return null; // 👈 이런 이상값 방지
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) return null;
        if (!url.startsWith("/")) return null;               // 내부 절대경로만
        if (url.startsWith("/my-info")) return null;         // 자기 자신으로 루프 금지
        if (url.contains("\r") || url.contains("\n")) return null;
        return url;
    }

    @GetMapping("/my-info")
    public String myInfo(Model model,
                         Authentication auth,
                         HttpSession session,
                         HttpServletRequest request) {

        // ✅ 돌아갈 경로 계산: ?returnTo= 우선, 없으면 Referer에서 추출
        String returnTo = sanitizeReturnTo(request.getParameter("returnTo"));
        if (returnTo == null) {
            String ref = request.getHeader("Referer");
            if (ref != null && !ref.contains("/my-info")) {
                try {
                    URI u = URI.create(ref);
                    String path = u.getPath();
                    String q = u.getQuery();
                    returnTo = sanitizeReturnTo(path + (q != null ? "?" + q : ""));
                } catch (Exception ignored) {}
            }
        }
        // 세션에 백업 저장(POST에서 폼이 비었을 때 사용)
        if (returnTo != null) session.setAttribute(RETURN_TO_KEY, returnTo);
        else session.removeAttribute(RETURN_TO_KEY);

        String username = resolveUsername(auth, session);
        if (username == null) {
            model.addAttribute("errorMessage", "로그인 후 이용해 주세요.");
            MyInfoForm empty = new MyInfoForm();
            empty.setReturnTo(returnTo);
            model.addAttribute("form", empty);
            model.addAttribute("returnTo", returnTo);
            return "my-info";
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        MyInfoForm form = new MyInfoForm();
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setAddress(user.getAddress());
        form.setReturnTo(returnTo); // ✅ 폼에도 심기

        model.addAttribute("form", form);
        model.addAttribute("returnTo", returnTo);
        return "my-info";
    }

    @PostMapping("/my-info")
    public String updateMyInfo(@Valid @ModelAttribute("form") MyInfoForm form,
                               BindingResult bindingResult,
                               Authentication auth,
                               HttpSession session,
                               Model model) {

        String username = resolveUsername(auth, session);
        if (username == null) {
            model.addAttribute("errorMessage", "로그인 후 이용해 주세요.");
            return "my-info";
        }

        if (bindingResult.hasErrors()) {
            return "my-info";
        }

        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            model.addAttribute("errorMessage", "사용자를 찾을 수 없습니다.");
            return "my-info";
        }

        User user = opt.get();

        // 비밀번호 변경(선택)
        boolean wantChange = !isBlank(form.getNewPassword());
        if (wantChange) {
            if (!form.getNewPassword().equals(form.getNewPasswordConfirm())) {
                bindingResult.rejectValue("newPasswordConfirm", "mismatch", "새 비밀번호와 확인이 일치하지 않습니다.");
                return "my-info";
            }
            if (isBlank(form.getCurrentPassword()) ||
                    !passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
                bindingResult.rejectValue("currentPassword", "invalid", "현재 비밀번호가 올바르지 않습니다.");
                return "my-info";
            }
            user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        }

        // 기본 정보 저장
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        userRepository.save(user);

        // ✅ 안전한 되돌아가기: 폼 → 세션 순서로 가져와 검사
        String candidate = !isBlank(form.getReturnTo()) ? form.getReturnTo()
                : (String) session.getAttribute(RETURN_TO_KEY);
        String target = sanitizeReturnTo(candidate);
        session.removeAttribute(RETURN_TO_KEY); // 일회성

        if (target != null) {
            return "redirect:" + target;
        }

        // fallback: 현재 페이지에 성공 메시지
        model.addAttribute("successMessage", "내 정보가 저장되었습니다.");
        model.addAttribute("form", form);
        return "my-info";
    }
}
