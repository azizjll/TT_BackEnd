package com.example.TT_BackEnd.util;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String subject = "Activation de votre compte";
        String message = "Cliquez sur ce lien pour activer votre compte : "
                + "http://localhost:8080/auth/verify?token=" + token;

        sendEmail(to, subject, message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Réinitialisation du mot de passe";
        String message = "Réinitialisation du mot de passe\n\n"
                + "Lien : http://localhost:4200/reset-password?token=" + token + "\n\n"
                + "Ou utilisez ce code : " + token;

        sendEmail(to, subject, message);
    }

    private void sendEmail(String to, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("azizchahlaoui7@gmail.com");
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }

    public void sendSaisonnierWelcomeEmail(String to, String nom,
                                           String motDePasse, String token) {
        String lienVerification = "http://localhost:8080/auth/verify?token=" + token;

        String contenu = """
        Bonjour %s,
        
        Votre candidature a été envoyée avec succès.
        
        Voici vos identifiants de connexion :
        📧 Email : %s
        🔑 Mot de passe  : %s
        
        Veuillez vérifier votre compte en cliquant sur ce lien :
        %s
        
        Ce lien expire dans 24h.
        """.formatted(nom, to, motDePasse, lienVerification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("azizchahlaoui7@gmail.com");
        message.setTo(to);
        message.setSubject("Bienvenue — Activation de votre compte saisonnier");
        message.setText(contenu);
        mailSender.send(message);
    }
}
