package com.example.TT_BackEnd.service;


import com.example.TT_BackEnd.dto.*;
import com.example.TT_BackEnd.entity.*;
import com.example.TT_BackEnd.repository.*;
import com.example.TT_BackEnd.util.EmailServiceImpl;
import com.example.TT_BackEnd.util.JwtUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmailServiceImpl emailService; // ton service mail
    private final RegionRepository regionRepository;

    // -------------------- SIGNUP --------------------
    public void signup(SignupRequest request) {
        Utilisateur user = new Utilisateur();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setCin(request.getCin());
        user.setTelephone(request.getTelephone());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        // Récupération de la région si fournie
        if (request.getRegionId() != null) {
            Region region = regionRepository.findById(request.getRegionId())
                    .orElseThrow(() -> new RuntimeException("Region non trouvée"));
            user.setRegion(region);
        }

        userRepository.save(user);

        // Création du token de vérification
        VerificationToken token = new VerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        verificationTokenRepository.save(token);

        // Envoi email
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }


    // -------------------- VERIFY EMAIL --------------------
    public void verifyToken(String tokenStr) {
        VerificationToken token = verificationTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token invalide"));
        if(token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }
        Utilisateur user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(token);
    }

    // -------------------- SIGNIN + JWT --------------------
    public String signin(SigninRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return jwtUtils.generateToken(userDetails);
    }

    // -------------------- FORGOT PASSWORD --------------------
    @Transactional
    public void forgotPassword(String email) {

        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        PasswordResetToken token = passwordResetTokenRepository
                .findByUser(user)
                .orElse(new PasswordResetToken());

        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(1));

        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    // -------------------- RESET PASSWORD --------------------
    public void resetPassword(NewPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if(token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        Utilisateur user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(token);
    }

    // -------------------- LOAD USER FOR SPRING SECURITY --------------------
    public UserDetails loadUserByUsername(String email) {
        Utilisateur utilisateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getPassword())
                .disabled(!utilisateur.getEnabled())
                .authorities(utilisateur.getRole().name())
                .build();
    }
}
