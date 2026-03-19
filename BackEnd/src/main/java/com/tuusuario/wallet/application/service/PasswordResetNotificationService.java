package com.tuusuario.wallet.application.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Envio de email para recuperacion de contrasena.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@wallet.local}")
    private String fromAddress;

    @Value("${app.password-reset.frontend-reset-url:http://localhost:5173}")
    private String frontendResetUrl;

    public boolean sendPasswordResetEmail(String email, String rawToken) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("No hay JavaMailSender configurado. Se omite envio de email de recuperacion para {}", email);
            return false;
        }

        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        String resetLink = frontendResetUrl + "?token=" + encodedToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Recuperacion de contrasena - Wallet Digital");
        message.setText(
                "Recibimos una solicitud para restablecer tu contrasena.\n\n"
                        + "Usa este enlace (expira en 30 minutos):\n"
                        + resetLink + "\n\n"
                        + "Si no solicitaste este cambio, ignora este correo."
        );

        try {
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Error enviando email de recuperacion a {}", email, ex);
            return false;
        }
    }
}
