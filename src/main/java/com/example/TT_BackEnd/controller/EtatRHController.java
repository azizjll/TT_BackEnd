package com.example.TT_BackEnd.controller;

import com.example.TT_BackEnd.dto.EtatRHDTO;
import com.example.TT_BackEnd.entity.StatutEtat;
import com.example.TT_BackEnd.service.EtatRHService;
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

    // ── RH_REGIONAL ──────────────────────────────────────────────
    @PostMapping("/rh/etat/upload")
    public ResponseEntity<?> uploadEtat(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== Upload reçu : " + file.getOriginalFilename());
            System.out.println("=== Taille : " + file.getSize());
            System.out.println("=== Content-Type : " + file.getContentType());

            return ResponseEntity.ok(etatRHService.uploadEtat(file));
        } catch (Exception e) {
            System.out.println("=== ERREUR uploadEtat : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rh/etat/mon-etat")
    public ResponseEntity<?> getMonEtat() {
        return ResponseEntity.ok(etatRHService.getMonEtat().orElse(null));
    }

    // ── ADMIN ─────────────────────────────────────────────────────
    @GetMapping("/admin/etats")
    public ResponseEntity<List<EtatRHDTO>> getAllEtats() {
        return ResponseEntity.ok(etatRHService.getAllEtatsCampagneActive());
    }

    @PatchMapping("/admin/etats/{id}/statut")
    public ResponseEntity<?> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutEtat statut) {
        return ResponseEntity.ok(etatRHService.changerStatut(id, statut));
    }
}