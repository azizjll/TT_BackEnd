package com.example.TT_BackEnd.util;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

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
        String message = "Bonjour,\n\n"
                + "Voici votre code de réinitialisation :\n\n"
                + "    " + token + "\n\n"
                + "Ce code est valable 15 minutes.\n"
                + "Si vous n'avez pas fait cette demande, ignorez cet email.";
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

    public void envoyerDemandeAutorisationJuilletAout(
            String prenomSaisonnier,
            String nomSaisonnier,
            String cin,
            String directionRH,
            String commentaire,
            List<String> emailsAdmins
    ) {
        String sujet = "📋 Demande d'autorisation — Juillet + Août — "
                + prenomSaisonnier + " " + nomSaisonnier;

        String corps = """
            Bonjour,

            Une demande d'autorisation pour un contrat Juillet + Août a été soumise.

            ── Informations du saisonnier ──
            Nom complet  : %s %s
            CIN          : %s
            Direction RH : %s
            Mois demandé : Juillet + Août

            ── Commentaire du RH ──
            %s

            Merci de traiter cette demande dans les meilleurs délais.

            Cordialement,
            Système de gestion des saisonniers
            """.formatted(
                prenomSaisonnier, nomSaisonnier,
                cin,
                directionRH,
                commentaire != null ? commentaire : "(aucun commentaire)"
        );

        for (String email : emailsAdmins) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("azizchahlaoui7@gmail.com");
            msg.setTo(email);
            msg.setSubject(sujet);
            msg.setText(corps);
            mailSender.send(msg);
        }
    }

    public void envoyerDemandeAutorisationQuotaParent(
            String prenomSaisonnier,
            String nomSaisonnier,
            String cin,
            String matriculeParent,
            String nomPrenomParent,
            int utilise,
            int autorises,
            String directionRH,
            String prenom,
            String nom,
            String commentaire,
            List<String> emailsAdmins
    ) {
        String sujet = "⚠️ Demande d'autorisation — Quota parent dépassé — "
                + prenomSaisonnier + " " + nomSaisonnier;

        String corps = """
        Bonjour,

        Une demande d'autorisation exceptionnelle a été soumise par un RH.
        Le matricule parent a dépassé son quota d'utilisations autorisées.

        ── Informations du saisonnier ──
        Nom complet      : %s %s
        CIN              : %s

        ── Informations du parent ──
        Nom & Prénom     : %s
        Matricule        : %s
        Utilisations     : %d / %d (quota dépassé)

        ── RH demandeur ──
        Nom complet      : %s %s
        Direction        : %s

        ── Commentaire du RH ──
        %s

        Merci de valider ou rejeter cette demande dans les meilleurs délais.

        Cordialement,
        Système de gestion des saisonniers
        """.formatted(
                prenomSaisonnier, nomSaisonnier,
                cin,
                nomPrenomParent,
                matriculeParent,
                utilise, autorises,
                prenom, nom,
                directionRH,
                commentaire != null ? commentaire : "(aucun commentaire)"
        );

        for (String email : emailsAdmins) {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("azizchahlaoui7@gmail.com");
            msg.setTo(email);
            msg.setSubject(sujet);
            msg.setText(corps);
            mailSender.send(msg);
        }
    }

    public void sendSuperAdminWelcomeEmail(String to, String nom, String prenom, Integer matricule) {
        String sujet = "🔐 Bienvenue — Vos identifiants SuperAdmin";

        String contenu = """
        Bonjour %s %s,

        Votre compte SuperAdmin a été créé avec succès.

        ── Vos identifiants de connexion ──
        📧 Email      : %s
        🪪 Matricule  : %d
        🔑 Mot de passe : %d

        ⚠️ Pour des raisons de sécurité, veuillez changer votre mot de passe dès votre première connexion.

        Cordialement,
        Système de gestion des saisonniers
        """.formatted(prenom, nom, to, matricule, matricule);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("azizchahlaoui7@gmail.com");
        message.setTo(to);
        message.setSubject(sujet);
        message.setText(contenu);
        mailSender.send(message);
    }
    public void sendWelcomeRHEmail(String to, String nom, String prenom, Integer matricule) {
        String sujet = "🎉 Bienvenue sur la plateforme de gestion des saisonniers";

        String contenu = """
        Bonjour %s %s,

        Votre compte a été créé avec succès sur la plateforme de gestion des saisonniers.

        ── Vos identifiants de connexion ──
        📧 Email      : %s
        🔑 Mot de passe : %d

        🔗 Accéder à la plateforme :
        http://localhost:4200/home-ge

        ⚠️ Votre mot de passe est votre matricule. Veuillez le changer dès votre première connexion.

        Cordialement,
        Système de gestion des saisonniers
        """.formatted(prenom, nom, to, matricule);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("azizchahlaoui7@gmail.com");
        message.setTo(to);
        message.setSubject(sujet);
        message.setText(contenu);
        mailSender.send(message);
    }

    public void sendCandidatureAccepteeEmail(String to, String prenomNom) {
        String sujet = "✅ Candidature acceptée — Tunisie Telecom";

        String contenu = """
        Bonjour %s,

        Nous avons le plaisir de vous informer que votre candidature a été acceptée.

        Vous ferez partie de notre équipe de saisonniers pour cette campagne.
        Vous serez contacté prochainement pour les détails de votre prise de poste.

        Bienvenue dans l'équipe ! 🎉

        Cordialement,
        Direction des Ressources Humaines
        Tunisie Telecom
        """.formatted(prenomNom);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("azizchahlaoui7@gmail.com");
        message.setTo(to);
        message.setSubject(sujet);
        message.setText(contenu);
        mailSender.send(message);
    }

    public void sendCandidatureRefuseeEmail(String to, String prenomNom) {
        String sujet = "❌ Candidature — Suite donnée à votre dossier";

        String contenu = """
        Bonjour %s,

        Nous vous remercions de l'intérêt que vous portez à Tunisie Telecom
        et du temps consacré à votre candidature.

        Malheureusement, nous ne sommes pas en mesure de donner suite
        à votre candidature pour cette campagne.

        Nous vous encourageons à renouveler votre candidature lors
        de nos prochaines campagnes de recrutement saisonnier.

        Cordialement,
        Direction des Ressources Humaines
        Tunisie Telecom
        """.formatted(prenomNom);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("azizchahlaoui7@gmail.com");
        message.setTo(to);
        message.setSubject(sujet);
        message.setText(contenu);
        mailSender.send(message);
    }

}
