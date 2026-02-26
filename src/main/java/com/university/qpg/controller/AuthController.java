package com.university.qpg.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.university.qpg.service.AppUserService;

@Controller
public class AuthController {

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!StringUtils.hasText(username)
                || !StringUtils.hasText(email)
                || !StringUtils.hasText(password)
                || !StringUtils.hasText(confirmPassword)) {
            model.addAttribute("error", "All fields are required.");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }

        try {
            appUserService.registerUser(username.trim(), email.trim().toLowerCase(), password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "signup";
        }
    }
}
