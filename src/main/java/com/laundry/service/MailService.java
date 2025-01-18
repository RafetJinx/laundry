package com.laundry.service;

import com.laundry.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.resetPassword.url}")
    private String resetPasswordUrl;

    public void sendResetPasswordEmail(User user, String token) {
        String link = resetPasswordUrl + "?token=" + token;

        Context context = new Context();
        context.setVariable("name", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        context.setVariable("resetLink", link);

        String htmlBody = templateEngine.process("reset-password-email.html", context);

        // send mail
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Request");
            helper.setText(htmlBody, true); // isHtml = true
        };
        mailSender.send(preparator);
    }
}

