package com.carrot.example.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RolePagesController {

    @GetMapping("/admin/dashboard")
    public String adminPage() { return "admin-dashboard"; }

    @GetMapping("/user/mypage")
    public String userPage() { return "user-mypage"; }
}
