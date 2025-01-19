package com.laundry.controller;

import com.laundry.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view/auth")
public class AuthViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        boolean valid = userService.isResetTokenValid(token);
        if (!valid) {
            return "invalid-reset-token";
        }

        model.addAttribute("resetToken", token);

        return "reset-password-form";
    }

}

