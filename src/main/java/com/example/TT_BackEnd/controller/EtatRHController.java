package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.EtatRHDTO;
import com.example.TT_BackEnd.entity.StatutEtat;
import com.example.TT_BackEnd.service.EtatRHService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EtatRHController {

    private final EtatRHService etatRHService;

    // =========================
    // UTILITAIRE
    // =========================
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isEmpty()) ? ip.split(",")[0] : request.getRemoteAddr();
    }

    private String getCurrentUserEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        return auth.getName();
    }

    // =========================
    // RH_REGIONAL
    // =========================
    @PostMapping("/rh/etat/upload")
    public ResponseEntity<?> uploadEtat(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            System.out.println("=== Upload reçu : " + file.getOriginalFilename());
            System.out.println("=== Taille : " + file.getSize());
            System.out.println("=== Content-Type : " + file.getContentType());

            String email = getCurrentUserEmail();
            return ResponseEntity.ok(etatRHService.uploadEtat(file, email, getClientIp(request)));
        } catch (Exception e) {
            System.out.println("=== ERREUR uploadEtat : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rh/etat/mon-etat")
    public ResponseEntity<?> getMonEtat(HttpServletRequest request) {
        String email = getCurrentUserEmail();
        return ResponseEntity.ok(etatRHService.getMonEtat(email, getClientIp(request)).orElse(null));
    }

    // =========================
    // ADMIN
    // =========================
    @GetMapping("/admin/etats")
    public ResponseEntity<List<EtatRHDTO>> getAllEtats(HttpServletRequest request) {
        String email = getCurrentUserEmail();
        return ResponseEntity.ok(etatRHService.getAllEtatsCampagneActive(email, getClientIp(request)));
    }

    @PatchMapping("/admin/etats/{id}/statut")
    public ResponseEntity<?> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutEtat statut,
            HttpServletRequest request) {

        String email = getCurrentUserEmail();
        return ResponseEntity.ok(etatRHService.changerStatut(id, statut, email, getClientIp(request)));
    }
}