package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.*;
import com.example.TT_BackEnd.entity.Region;
import com.example.TT_BackEnd.entity.RoleType;
import com.example.TT_BackEnd.entity.Utilisateur;

import com.example.TT_BackEnd.repository.RegionRepository;
import com.example.TT_BackEnd.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;
    private final RegionRepository regionRepository;


    @GetMapping("/regions")
    public List<RegionDTO> getRegions() {
        return regionRepository.findAll()
                .stream()
                .map(r -> new RegionDTO(r.getId(), r.getNom()))
                .toList();
    }
    @GetMapping("/roles")
    public List<String> getRoles() {
        return Arrays.stream(RoleType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("Inscription réussie ! Vérifiez votre email.");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        authService.verifyToken(token);
        return ResponseEntity.ok("Compte activé !");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninRequest request) {
        String jwt = authService.signin(request);
        return ResponseEntity.ok(Map.of("token", jwt));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordResetRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Lien de réinitialisation envoyé par email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody NewPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe réinitialisé avec succès !"
        ));
    }
}
