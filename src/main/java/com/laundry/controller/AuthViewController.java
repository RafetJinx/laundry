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

    /**
     * GET /reset-password?token=...
     * Renders a Thymeleaf page "reset-password-form.html" if token is valid;
     * otherwise shows "invalid-reset-token.html".
     *
     * This mapping is for the user clicking the link from the email (which does a GET).
     */
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        // 1) Check if token is valid
        boolean valid = userService.isResetTokenValid(token);
        if (!valid) {
            // Return an error page (invalid-reset-token.html)
            return "invalid-reset-token";
        }

        // 2) If valid, pass the token to the thymeleaf model
        model.addAttribute("resetToken", token);

        // 3) Show the form where user can enter a new password
        return "reset-password-form";
    }

}

