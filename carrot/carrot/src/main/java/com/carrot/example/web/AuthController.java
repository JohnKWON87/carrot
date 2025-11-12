package com.carrot.example.web;

import com.carrot.example.user.*;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@Validated
public class AuthController {

    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;

    public AuthController(UserAccountRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") @Validated RegisterForm form, Model model) {

        if (repo.existsByEmail(form.getEmail())) {
            model.addAttribute("error", "이미 존재하는 이메일입니다.");
            return "register";
        }
        Role role = "ADMIN".equalsIgnoreCase(form.getRole()) ? Role.ROLE_ADMIN : Role.ROLE_USER;

        UserAccount ua = UserAccount.builder()
                .email(form.getEmail())
                .password(encoder.encode(form.getPassword()))
                .roles(Set.of(role))
                .build();
        repo.save(ua);
        return "redirect:/login?registered";
    }

    @Data
    public static class RegisterForm {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;
        // "ADMIN" 또는 "USER"
        private String role = "USER";
    }
}
